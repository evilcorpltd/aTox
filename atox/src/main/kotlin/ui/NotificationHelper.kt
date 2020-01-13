package ltd.evilcorp.atox.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_TEXT_LINES
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.domain.tox.PublicKey
import javax.inject.Inject
import javax.inject.Singleton

private const val MESSAGE = "aTox messages"
private const val FRIEND_REQUEST = "aTox friend requests"

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

        notifier.createNotificationChannels(listOf(messageChannel, friendRequestChannel))
    }

    fun dismissNotifications(publicKey: PublicKey) = notifier.cancel(publicKey.string().hashCode())

    fun showMessageNotification(contact: Contact, message: String) {
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
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val messages = notifier.activeNotifications.find { it.notification.group == contact.publicKey }
                ?.notification?.extras?.getCharSequenceArray(EXTRA_TEXT_LINES)?.toMutableList()
                ?: ArrayList<CharSequence>()

            messages.add(message)

            val style = NotificationCompat.InboxStyle()
            messages.forEach {
                style.addLine(it)
            }

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
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setAutoCancel(true)

        notifier.notify(friendRequest.publicKey.hashCode(), notificationBuilder.build())
    }
}
