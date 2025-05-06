// SPDX-FileCopyrightText: 2022 aTox contributors
// SPDX-FileCopyrightText: 2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import javax.inject.Inject
import javax.inject.Singleton

private const val PROXIMITY_SCREEN_OFF_WAKELOCK_TAG = "atox:ProximityScreenOff"

@Singleton
class ProximityScreenOff @Inject constructor(context: Context) {
    private val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var screenOffWakeLock: PowerManager.WakeLock? = null

    @SuppressLint("WakelockTimeout")
    fun acquire() {
        if (screenOffWakeLock == null) {
            screenOffWakeLock =
                powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, PROXIMITY_SCREEN_OFF_WAKELOCK_TAG)
            screenOffWakeLock?.acquire()
        }
    }

    fun release() {
        screenOffWakeLock?.apply {
            if (isHeld) release()
            screenOffWakeLock = null
        }
    }
}
