package ltd.evilcorp.atox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.observe
import dagger.android.AndroidInjection
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.domain.tox.Tox
import javax.inject.Inject

private const val TAG = "ToxService"

class ToxService : LifecycleService() {
    private val channelId = "ToxService"
    private val notificationId = 1984

    private var connectionStatus = ConnectionStatus.None

    @Inject
    lateinit var tox: Tox

    @Inject
    lateinit var toxStarter: ToxStarter

    @Inject
    lateinit var userRepository: UserRepository

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val friendRequestChannel = NotificationChannel(
            channelId,
            "Tox Service",
            NotificationManager.IMPORTANCE_LOW
        )

        val notifier = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifier.createNotificationChannels(listOf(friendRequestChannel))
    }

    private fun subTextFor(status: ConnectionStatus) = when (status) {
        ConnectionStatus.None -> getText(R.string.atox_offline)
        ConnectionStatus.TCP -> getText(R.string.atox_connected_with_tcp)
        ConnectionStatus.UDP -> getText(R.string.atox_connected_with_udp)
    }

    override fun onCreate() {
        AndroidInjection.inject(this)

        super.onCreate()

        if (!tox.started) {
            if (!toxStarter.tryLoadTox()) {
                Log.e(TAG, "Tox service started without a Tox save")
                stopSelf()
            }
        }

        createNotificationChannel()

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        // TODO(robinlinden): setContentTitle and setContentText for n messages received?
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setSubText(subTextFor(connectionStatus))
            .build()

        startForeground(notificationId, notification)

        userRepository.get(tox.publicKey.string()).observe(this) { user ->
            if (user.connectionStatus == connectionStatus) return@observe
            connectionStatus = user.connectionStatus

            val statusUpdateNotification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setSubText(subTextFor(connectionStatus))
                .build()

            val notifier = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifier.notify(notificationId, statusUpdateNotification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        tox.stop()
    }
}
