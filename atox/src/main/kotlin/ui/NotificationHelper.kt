// SPDX-FileCopyrightText: 2019-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import ltd.evilcorp.atox.Action
import ltd.evilcorp.atox.ActionReceiver
import ltd.evilcorp.atox.KEY_ACTION
import ltd.evilcorp.atox.KEY_CONTACT_PK
import ltd.evilcorp.atox.KEY_TEXT_REPLY
import ltd.evilcorp.atox.PendingIntentCompat
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.atox.ui.chat.FOCUS_ON_MESSAGE_BOX
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.tox.PublicKey
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

private const val MESSAGE = "aTox messages"
private const val FRIEND_REQUEST = "aTox friend requests"
private const val CALL = "aTox calls"

class NotificationHelper(override val di: DI) : DIAware {
    private val context: Context by instance()

    private val notifier = NotificationManagerCompat.from(context)
    private val notifierOld = context.getSystemService<NotificationManager>()!!

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val messageChannel = NotificationChannelCompat.Builder(MESSAGE, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(context.getString(R.string.messages))
            .setDescription(context.getString(R.string.messages_incoming))
            .build()

        val friendChannel = NotificationChannelCompat.Builder(FRIEND_REQUEST, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(context.getString(R.string.friend_requests))
            .build()

        // We can't use getActualDefaultRingtoneUri as Samsung requires the WRITE_SETTINGS permission for that. :D
        // See: https://github.com/evilcorpltd/aTox/issues/958
        val ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val callChannelBuilder = NotificationChannelCompat.Builder(CALL, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(context.getString(R.string.calls))
            .setVibrationEnabled(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()
            callChannelBuilder.setSound(ringtone, audioAttributes)
        } else {
            callChannelBuilder.setSound(ringtone, null)
        }

        val callChannel = callChannelBuilder.build()

        notifier.createNotificationChannelsCompat(listOf(messageChannel, friendChannel, callChannel))
    }

    fun dismissNotifications(publicKey: PublicKey) = notifier.cancel(publicKey.string().hashCode())

    private val circleTransform = object : Transformation {
        override fun transform(bitmap: Bitmap): Bitmap {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)

            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawCircle(bitmap.width / 2.0f, bitmap.height / 2.0f, bitmap.width / 2.0f, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
            if (bitmap != output) {
                bitmap.recycle()
            }
            return output
        }

        override fun key() = "circleTransform"
    }

    fun showMessageNotification(
        contact: Contact,
        message: String,
        outgoing: Boolean = false,
        silent: Boolean = outgoing,
    ) {
        val notificationBuilder = NotificationCompat.Builder(context, MESSAGE)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(contact.name.ifEmpty { context.getText(R.string.contact_default_name) })
            .setContentText(message)
            .setContentIntent(deepLinkToChat(PublicKey(contact.publicKey)))
            .setAutoCancel(true)
            .setSilent(silent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val icon = if (contact.avatarUri.isNotEmpty()) {
                IconCompat.createWithBitmap(Picasso.get().load(contact.avatarUri).transform(circleTransform).get())
            } else null

            val chatPartner = Person.Builder()
                .setName(contact.name.ifEmpty { context.getText(R.string.contact_default_name) })
                .setKey(if (outgoing) "myself" else contact.publicKey)
                .setIcon(icon)
                .setImportant(true)
                .build()

            val style =
                notifierOld.activeNotifications.find { it.notification.group == contact.publicKey }?.notification?.let {
                    NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(it)
                } ?: NotificationCompat.MessagingStyle(chatPartner)

            style.messages.add(
                NotificationCompat.MessagingStyle.Message(message, System.currentTimeMillis(), chatPartner)
            )

            notificationBuilder
                .setStyle(style)
                .setGroup(contact.publicKey)
        }

        // I can't find it in the documentation for RemoteInput or anything, but per
        // https://developer.android.com/training/notify-user/build-notification#reply-action
        // support for this was only introduced in Android N (API 24).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationBuilder.addAction(
                NotificationCompat.Action
                    .Builder(
                        IconCompat.createWithResource(context, R.drawable.ic_send),
                        context.getString(R.string.reply),
                        PendingIntentCompat.getBroadcast(
                            context,
                            contact.publicKey.hashCode(),
                            Intent(context, ActionReceiver::class.java).putExtra(KEY_CONTACT_PK, contact.publicKey),
                            PendingIntent.FLAG_UPDATE_CURRENT,
                            mutable = true,
                        )
                    )
                    .addRemoteInput(
                        RemoteInput.Builder(KEY_TEXT_REPLY)
                            .setLabel(context.getString(R.string.message))
                            .build()
                    )
                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                    .setAllowGeneratedReplies(true)
                    .build()
            )
        } else {
            notificationBuilder.addAction(
                NotificationCompat.Action
                    .Builder(
                        IconCompat.createWithResource(context, R.drawable.ic_send),
                        context.getString(R.string.reply),
                        deepLinkToChat(PublicKey(contact.publicKey), focusMessageBox = true)
                    )
                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                    .build()
            )
        }

        notificationBuilder.addAction(
            NotificationCompat.Action.Builder(
                null,
                context.getString(R.string.mark_as_read),
                PendingIntentCompat.getBroadcast(
                    context,
                    "${contact.publicKey}_mark_as_read".hashCode(),
                    Intent(context, ActionReceiver::class.java)
                        .putExtra(KEY_CONTACT_PK, contact.publicKey)
                        .putExtra(KEY_ACTION, Action.MarkAsRead),
                    PendingIntent.FLAG_UPDATE_CURRENT,
                )
            ).setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ).build()
        )

        notifier.notify(contact.publicKey.hashCode(), notificationBuilder.build())
    }

    fun showFriendRequestNotification(friendRequest: FriendRequest, silent: Boolean) {
        val notificationBuilder = NotificationCompat.Builder(context, FRIEND_REQUEST)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .setContentTitle(context.getString(R.string.friend_request_from, friendRequest.publicKey))
            .setContentText(friendRequest.message)
            .setContentIntent(
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.contactListFragment)
                    .createPendingIntent()
            )
            .setAutoCancel(true)
            .setSilent(silent)

        notifier.notify(friendRequest.publicKey.hashCode(), notificationBuilder.build())
    }

    fun dismissCallNotification(pk: PublicKey) =
        notifier.cancel(pk.string().hashCode() + CALL.hashCode())

    fun showOngoingCallNotification(contact: Contact) {
        dismissCallNotification(PublicKey(contact.publicKey))
        val notificationBuilder = NotificationCompat.Builder(context, CALL)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle(context.getString(R.string.ongoing_call))
            .setContentText(
                context.getString(
                    R.string.in_call_with,
                    contact.name.ifEmpty { context.getString(R.string.contact_default_name) }
                )
            )
            .setUsesChronometer(true)
            .setWhen(System.currentTimeMillis())
            .setContentIntent(
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .addDestination(R.id.chatFragment, bundleOf(CONTACT_PUBLIC_KEY to contact.publicKey))
                    .addDestination(R.id.callFragment, bundleOf(CONTACT_PUBLIC_KEY to contact.publicKey))
                    .createPendingIntent()
            )
            .addAction(
                NotificationCompat.Action
                    .Builder(
                        IconCompat.createWithResource(context, R.drawable.ic_call_end),
                        context.getString(R.string.end_call),
                        PendingIntentCompat.getBroadcast(
                            context,
                            "${contact.publicKey}_end_call".hashCode(),
                            Intent(context, ActionReceiver::class.java)
                                .putExtra(KEY_CONTACT_PK, contact.publicKey)
                                .putExtra(KEY_ACTION, Action.CallEnd),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                    .build()
            )
            .setOngoing(true)
            .setSilent(true)

        notifier.notify(contact.publicKey.hashCode() + CALL.hashCode(), notificationBuilder.build())
    }

    fun showPendingCallNotification(status: UserStatus, c: Contact) {
        val notification = NotificationCompat.Builder(context, CALL)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle(context.getString(R.string.incoming_call))
            .setContentText(context.getString(R.string.incoming_call_from, c.name))
            .setContentIntent(deepLinkToChat(PublicKey(c.publicKey)))
            .addAction(
                NotificationCompat.Action
                    .Builder(
                        IconCompat.createWithResource(context, R.drawable.ic_call),
                        context.getString(R.string.accept),
                        PendingIntentCompat.getBroadcast(
                            context,
                            "${c.publicKey}_accept_call".hashCode(),
                            Intent(context, ActionReceiver::class.java)
                                .putExtra(KEY_CONTACT_PK, c.publicKey)
                                .putExtra(KEY_ACTION, Action.CallAccept),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_CALL)
                    .build()
            )
            .addAction(
                NotificationCompat.Action
                    .Builder(
                        IconCompat.createWithResource(context, R.drawable.ic_not_interested),
                        context.getString(R.string.reject),
                        PendingIntentCompat.getBroadcast(
                            context,
                            "${c.publicKey}_reject_call".hashCode(),
                            Intent(context, ActionReceiver::class.java)
                                .putExtra(KEY_CONTACT_PK, c.publicKey)
                                .putExtra(KEY_ACTION, Action.CallReject),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                    .build()
            )
            .setDeleteIntent(
                PendingIntentCompat.getBroadcast(
                    context,
                    "${c.publicKey}_ignore_call".hashCode(),
                    Intent(context, ActionReceiver::class.java)
                        .putExtra(KEY_CONTACT_PK, c.publicKey)
                        .putExtra(KEY_ACTION, Action.CallIgnore),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setSound(RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE))
            .setSilent(status == UserStatus.Busy)
            .build()
            .apply {
                flags = flags.or(NotificationCompat.FLAG_INSISTENT)
            }

        notifier.notify(c.publicKey.hashCode() + CALL.hashCode(), notification)
    }

    private fun deepLinkToChat(publicKey: PublicKey, focusMessageBox: Boolean = false) = NavDeepLinkBuilder(context)
        .setGraph(R.navigation.nav_graph)
        .setDestination(R.id.chatFragment)
        .setArguments(
            bundleOf(
                CONTACT_PUBLIC_KEY to publicKey.string(),
                FOCUS_ON_MESSAGE_BOX to focusMessageBox,
            )
        )
        .createPendingIntent()
}
