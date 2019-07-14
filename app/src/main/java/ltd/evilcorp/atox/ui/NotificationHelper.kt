package ltd.evilcorp.atox.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.vo.Contact
import javax.inject.Inject
import javax.inject.Singleton

private const val MESSAGE_CHANNEL = "aTox messages"

@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {
    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val name = context.getString(R.string.messages)
        val descriptionText = context.getString(R.string.messages_incoming)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(MESSAGE_CHANNEL, name, importance).apply {
            description = descriptionText
        }

        val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifier.createNotificationChannel(channel)
    }

    fun showMessageNotification(contact: Contact, message: String) {
        val notificationBuilder = NotificationCompat.Builder(context, MESSAGE_CHANNEL)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(contact.name)
            .setContentText(message)
            .setCategory(Notification.CATEGORY_MESSAGE)

        val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifier.notify(contact.publicKey.hashCode(), notificationBuilder.build())
    }
}
