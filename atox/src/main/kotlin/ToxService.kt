package ltd.evilcorp.atox

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.asLiveData
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule
import kotlinx.coroutines.flow.filter
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxSaveStatus

private const val TAG = "ToxService"

class ToxService : LifecycleService() {
    private val channelId = "ToxService"
    private val notificationId = 1984

    private var connectionStatus = ConnectionStatus.None

    private val notifier by lazy { getSystemService<NotificationManager>()!! }
    private var bootstrapTimer = Timer()

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

        notifier.createNotificationChannels(listOf(friendRequestChannel))
    }

    private fun subTextFor(status: ConnectionStatus) = when (status) {
        ConnectionStatus.None -> getText(R.string.atox_offline)
        ConnectionStatus.TCP -> getText(R.string.atox_connected_with_tcp)
        ConnectionStatus.UDP -> getText(R.string.atox_connected_with_udp)
    }

    private fun notificationFor(status: ConnectionStatus): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            builder.setSubText(subTextFor(status))
        } else {
            builder.setContentTitle(getString(R.string.tox_service_running))
            builder.setContentText(subTextFor(status))
        }

        return builder.build()
    }

    override fun onCreate() {
        (application as App).component.inject(this)

        super.onCreate()

        if (!tox.started) {
            if (toxStarter.tryLoadTox() != ToxSaveStatus.Ok) {
                Log.e(TAG, "Tox service started without a Tox save")
                stopSelf()
            }
        }

        createNotificationChannel()
        startForeground(notificationId, notificationFor(connectionStatus))

        userRepository.get(tox.publicKey.string())
            .filter { user: User? -> user != null }.asLiveData().observe(this) { user ->
                if (user.connectionStatus == connectionStatus) return@observe
                connectionStatus = user.connectionStatus
                notifier.notify(notificationId, notificationFor(connectionStatus))
                if (connectionStatus == ConnectionStatus.None) {
                    Log.i(TAG, "Gone offline, scheduling bootstrap")
                    bootstrapTimer.schedule(60_000) {
                        Log.i(TAG, "Been offline for too long, bootstrapping")
                        tox.isBootstrapNeeded = true
                    }
                } else {
                    Log.i(TAG, "Online, cancelling bootstrap")
                    bootstrapTimer.cancel()
                    bootstrapTimer = Timer()
                }
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
