// SPDX-FileCopyrightText: 2019-2024 Robin Lindén <dev@robinlinden.eu>
// SPDX-FileCopyrightText: 2021-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.domain.feature.CallManager
import ltd.evilcorp.domain.feature.CallState
import ltd.evilcorp.domain.feature.FriendRequestManager
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxSaveStatus

private const val TAG = "ToxService"
private const val NOTIFICATION_ID = 1984
private const val BOOTSTRAP_INTERVAL_MS = 60_000L

class ToxService : LifecycleService() {
    private val channelId = "ToxService"

    private var connectionStatus: ConnectionStatus? = null

    private val notifier by lazy { NotificationManagerCompat.from(this) }
    private var bootstrapTimer = Timer()

    private val knownFriendRequests = mutableSetOf<FriendRequest>()

    @Inject
    lateinit var tox: Tox

    @Inject
    lateinit var toxStarter: ToxStarter

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var callManager: CallManager

    @Inject
    lateinit var friendRequestManager: FriendRequestManager

    @Inject
    lateinit var proximityScreenOff: ProximityScreenOff

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName("Tox Service")
            .build()

        notifier.createNotificationChannel(channel)
    }

    private fun subTextFor(status: ConnectionStatus) = when (status) {
        ConnectionStatus.None -> getText(R.string.atox_offline)
        ConnectionStatus.TCP -> getText(R.string.atox_connected_with_tcp)
        ConnectionStatus.UDP -> getText(R.string.atox_connected_with_udp)
    }

    private fun notificationFor(status: ConnectionStatus?): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntentCompat.getActivity(this, 0, notificationIntent, 0)
            }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
            .setContentIntent(pendingIntent)
            .setContentTitle(getString(R.string.tox_service_running))

        if (status != null) {
            // Either we haven't received a status from Tox yet, or we don't
            // have notification permissions meaning we wouldn't be able to
            // update a status if we showed one.
            builder.setContentText(subTextFor(status))
        }

        return builder.build()
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

        val canPostNotifications = ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!canPostNotifications) {
            Log.w(TAG, "Notifications disallowed")
        }

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notificationFor(connectionStatus),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notificationFor(connectionStatus))
        }

        lifecycleScope.launch(Dispatchers.Default) {
            userRepository.get(tox.publicKey.string())
                .filterNotNull()
                .filter { it.connectionStatus != connectionStatus }
                .flowWithLifecycle(lifecycle)
                .collect { user ->
                    connectionStatus = user.connectionStatus
                    if (canPostNotifications) {
                        notifier.notify(NOTIFICATION_ID, notificationFor(connectionStatus))
                    }

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

        lifecycleScope.launch(Dispatchers.Default) {
            friendRequestManager.getAll()
                .filterNotNull()
                .flowWithLifecycle(lifecycle)
                .collect { friendRequests ->
                    val finishedFriendRequests = knownFriendRequests.minus(friendRequests.toSet())
                    finishedFriendRequests.forEach {
                        knownFriendRequests.remove(it)
                        notifier.cancel(it.publicKey.hashCode())
                    }
                    knownFriendRequests.addAll(friendRequests)
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
