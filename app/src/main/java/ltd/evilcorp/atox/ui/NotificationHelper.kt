package ltd.evilcorp.atox.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.activity.ChatActivity
import ltd.evilcorp.atox.activity.ContactListActivity
import ltd.evilcorp.atox.vo.Contact
import ltd.evilcorp.atox.vo.FriendRequest
import javax.inject.Inject
import javax.inject.Singleton

private const val MESSAGE = "aTox messages"
private const val FRIEND_REQUEST = "aTox friend requests"

@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {
    private val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

    fun showMessageNotification(contact: Contact, message: String) {
        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra("publicKey", contact.publicKey)
        }

        val notificationBuilder = NotificationCompat.Builder(context, MESSAGE)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(contact.name)
            .setContentText(message)
            .setContentIntent(PendingIntent.getActivity(context, contact.publicKey.hashCode(), intent, 0))
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setAutoCancel(true)

        notifier.notify(contact.publicKey.hashCode(), notificationBuilder.build())
    }

    fun showFriendRequestNotification(friendRequest: FriendRequest) {
        val intent = Intent(context, ContactListActivity::class.java)

        val notificationBuilder = NotificationCompat.Builder(context, FRIEND_REQUEST)
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .setContentTitle(context.getString(R.string.friend_request_from, friendRequest.publicKey))
            .setContentText(friendRequest.message)
            .setContentIntent(PendingIntent.getActivity(context, friendRequest.publicKey.hashCode(), intent, 0))
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setAutoCancel(true)

        notifier.notify(friendRequest.publicKey.hashCode(), notificationBuilder.build())
    }
}
