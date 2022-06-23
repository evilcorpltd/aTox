// SPDX-FileCopyrightText: 2019-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.domain.feature.CallManager
import ltd.evilcorp.domain.feature.CallState
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxSaveStatus
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule

private const val TAG = "ToxService"
private const val NOTIFICATION_ID = 1984
private const val BOOTSTRAP_INTERVAL_MS = 60_000L

class ToxService : LifecycleService() {
    private val channelId = "ToxService"

    private var connectionStatus: ConnectionStatus? = null

    private val notifier by lazy { NotificationManagerCompat.from(this) }
    private var bootstrapTimer = Timer()

    @Inject
    lateinit var tox: Tox

    @Inject
    lateinit var toxStarter: ToxStarter

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var callManager: CallManager

    @Inject
    lateinit var proximityScreenOff: ProximityScreenOff

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName("Tox Service")
            .build()

        notifier.createNotificationChannel(channel)
    }

    private fun subTextFor(status: ConnectionStatus?) = when (status) {
        null, ConnectionStatus.None -> getText(R.string.atox_offline)
        ConnectionStatus.TCP -> getText(R.string.atox_connected_with_tcp)
        ConnectionStatus.UDP -> getText(R.string.atox_connected_with_udp)
    }

    private fun notificationFor(status: ConnectionStatus?): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntentCompat.getActivity(this, 0, notificationIntent, 0)
            }

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
            .setContentIntent(pendingIntent)
            .setContentTitle(getString(R.string.tox_service_running))
            .setContentText(subTextFor(status))
            .build()
    }

    override fun onCreate() {
        (application as App).component.inject(this)

        super.onCreate()

        if (!tox.started) {
            if (toxStarter.tryLoadTox(null) != ToxSaveStatus.Ok) {
                Log.e(TAG, "Tox service started without a Tox save")
                stopSelf()
            }
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationFor(connectionStatus))

        lifecycleScope.launch(Dispatchers.Default) {
            userRepository.get(tox.publicKey.string())
                .filterNotNull()
                .filter { it.connectionStatus != connectionStatus }
                .flowWithLifecycle(lifecycle)
                .collect { user ->
                    connectionStatus = user.connectionStatus
                    notifier.notify(NOTIFICATION_ID, notificationFor(connectionStatus))
                    if (connectionStatus == ConnectionStatus.None) {
                        Log.i(TAG, "Gone offline, scheduling bootstrap")
                        bootstrapTimer.schedule(BOOTSTRAP_INTERVAL_MS, BOOTSTRAP_INTERVAL_MS) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lifecycleScope.launch {
                callManager.inCall.collect {
                    if (it is CallState.InCall) {
                        if (!callManager.speakerphoneOn) {
                            proximityScreenOff.acquire()
                        }
                    } else {
                        proximityScreenOff.release()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        bootstrapTimer.cancel()
        tox.stop()
    }
}
