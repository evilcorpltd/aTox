package ltd.evilcorp.atox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.android.AndroidInjection
import ltd.evilcorp.atox.tox.Tox
import ltd.evilcorp.atox.tox.ToxStarter
import javax.inject.Inject

private const val TAG = "ToxService"

class ToxService : Service() {
    private val channelId = "ToxService"
    private val notificationId = 1984

    @Inject
    lateinit var tox: Tox

    @Inject
    lateinit var toxStarter: ToxStarter

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

    override fun onCreate() {
        AndroidInjection.inject(this)

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

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(notificationId, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        tox.stop()
    }
}
