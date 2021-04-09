package ltd.evilcorp.atox.ui

import android.app.Notification
import android.app.NotificationChannel
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
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import javax.inject.Inject
import javax.inject.Singleton
import ltd.evilcorp.atox.KEY_CONTACT_PK
import ltd.evilcorp.atox.KEY_TEXT_REPLY
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.ReplyReceiver
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.domain.tox.PublicKey

private const val MESSAGE = "aTox messages"
private const val FRIEND_REQUEST = "aTox friend requests"
private const val CALL = "aTox call"

@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {
    private val notifier = context.getSystemService<NotificationManager>()!!

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val messageChannel = NotificationChannel(
            MESSAGE,
            context.getString(R.string.messages),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.messages_incoming)
        }

        val friendRequestChannel = NotificationChannel(
            FRIEND_REQUEST,
            context.getString(R.string.friend_requests),
            NotificationManager.IMPORTANCE_HIGH
        )

        val callChannel = NotificationChannel(
            CALL,
            context.getString(R.string.calls),
            NotificationManager.IMPORTANCE_HIGH
        )

        notifier.createNotificationChannels(listOf(messageChannel, friendRequestChannel, callChannel))
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

    fun showMessageNotification(contact: Contact, message: String, outgoing: Boolean = false) {
        val notificationBuilder = NotificationCompat.Builder(context, MESSAGE)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(contact.name)
            .setContentText(message)
            .setContentIntent(
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.chatFragment)
                    .setArguments(bundleOf(CONTACT_PUBLIC_KEY to contact.publicKey))
                    .createPendingIntent()
            )
            .setAutoCancel(true)
            .addAction(
                NotificationCompat.Action
                    .Builder(
                        IconCompat.createWithResource(context, R.drawable.send),
                        context.getString(R.string.reply),
                        PendingIntent.getBroadcast(
                            context,
                            contact.publicKey.hashCode(),
                            Intent(context, ReplyReceiver::class.java).putExtra(KEY_CONTACT_PK, contact.publicKey),
                            PendingIntent.FLAG_UPDATE_CURRENT
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

        if (outgoing) {
            notificationBuilder.setSilent(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_MESSAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val icon = if (contact.avatarUri.isNotEmpty()) {
                IconCompat.createWithBitmap(Picasso.get().load(contact.avatarUri).transform(circleTransform).get())
            } else null

            val chatPartner = Person.Builder()
                .setName(contact.name)
                .setKey(if (outgoing) "myself" else contact.publicKey)
                .setIcon(icon)
                .setImportant(true)
                .build()

            val style =
                notifier.activeNotifications.find { it.notification.group == contact.publicKey }?.notification?.let {
                    NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(it)
                } ?: NotificationCompat.MessagingStyle(chatPartner)

            style.messages.add(
                NotificationCompat.MessagingStyle.Message(message, System.currentTimeMillis(), chatPartner)
            )

            notificationBuilder
                .setStyle(style)
                .setGroup(contact.publicKey)
        }

        notifier.notify(contact.publicKey.hashCode(), notificationBuilder.build())
    }

    fun showFriendRequestNotification(friendRequest: FriendRequest) {
        val notificationBuilder = NotificationCompat.Builder(context, FRIEND_REQUEST)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_MESSAGE)
        }

        notifier.notify(friendRequest.publicKey.hashCode(), notificationBuilder.build())
    }

    fun dismissCallNotification(contact: Contact) =
        notifier.cancel(contact.publicKey.hashCode() + CALL.hashCode())

    fun showCallNotification(contact: Contact) {
        val notificationBuilder = NotificationCompat.Builder(context, FRIEND_REQUEST)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle(context.getString(R.string.ongoing_call))
            .setContentText(context.getString(R.string.in_call_with, contact.name))
            .setUsesChronometer(true)
            .setWhen(System.currentTimeMillis())
            .setContentIntent(
                NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.callFragment)
                    .setArguments(bundleOf(CONTACT_PUBLIC_KEY to contact.publicKey))
                    .createPendingIntent()
            )
            .setOngoing(true)
            .setSilent(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_CALL)
        }

        notifier.notify(contact.publicKey.hashCode() + CALL.hashCode(), notificationBuilder.build())
    }
}
