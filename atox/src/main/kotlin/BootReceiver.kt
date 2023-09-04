// SPDX-FileCopyrightText: 2020-2023 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import javax.inject.Inject
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.domain.tox.ToxSaveStatus

private const val ENCRYPTED = "aTox profile encrypted"
private const val TAG = "BootReceiver"

class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var toxStarter: ToxStarter

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            (context.applicationContext as App).component.inject(this)
            if (toxStarter.tryLoadTox(null) == ToxSaveStatus.Encrypted) {
                Log.i(TAG, "Telling the user to unlock their profile")
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.w(TAG, "Missing notify-permission")
                    return
                }

                val channel = NotificationChannelCompat.Builder(ENCRYPTED, NotificationManagerCompat.IMPORTANCE_HIGH)
                    .setName(context.getString(R.string.atox_profile_locked))
                    .setDescription(context.getString(R.string.channel_profile_locked_explanation))
                    .build()
                val notification = NotificationCompat.Builder(context, ENCRYPTED)
                    .setContentTitle(context.getString(R.string.atox_profile_locked))
                    .setContentText(context.getString(R.string.tap_to_unlock_and_start_atox))
                    .setContentIntent(
                        NavDeepLinkBuilder(context)
                            .setGraph(R.navigation.nav_graph)
                            .setDestination(R.id.contactListFragment)
                            .createPendingIntent(),
                    )
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setAutoCancel(true)
                    .build()
                val notifier = NotificationManagerCompat.from(context)
                notifier.createNotificationChannel(channel)
                notifier.notify(ENCRYPTED.hashCode(), notification)
            }
        }
    }
}
