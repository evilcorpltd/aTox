// SPDX-FileCopyrightText: 2020-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.domain.tox.ToxSaveStatus
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

private const val ENCRYPTED = "aTox profile encrypted"

class BootReceiver : BroadcastReceiver(), DIAware {
    override lateinit var di: DI

    private val toxStarter: ToxStarter by instance()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            di = (context.applicationContext as DIAware).di
            if (toxStarter.tryLoadTox(null) == ToxSaveStatus.Encrypted) {
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
                            .createPendingIntent()
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
