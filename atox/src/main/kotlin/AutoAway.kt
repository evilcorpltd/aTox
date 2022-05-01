// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import android.util.Log
import java.util.Timer
import kotlin.concurrent.schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.settings.Settings
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.Tox
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

private const val TAG = "AutoAway"

class AutoAway(override val di: DI) : DIAware {
    private val scope: CoroutineScope by instance()
    private val settings: Settings by instance()
    private val userManager: UserManager by instance()
    private val tox: Tox by instance()

    private var awayTimer = Timer()
    private var autoAway = false

    fun onBackground() {
        if (!settings.autoAwayEnabled) return

        Log.i(TAG, "In background, scheduling away")
        awayTimer.schedule(settings.autoAwaySeconds * 1_000) {
            scope.launch {
                if (tox.getStatus() != UserStatus.None) return@launch
                Log.i(TAG, "Setting away")
                userManager.setStatus(UserStatus.Away)
                autoAway = true
            }
        }
    }

    fun onForeground() {
        if (!settings.autoAwayEnabled) return
        Log.i(TAG, "In foreground, canceling away")
        awayTimer.cancel()
        awayTimer = Timer()
        if (autoAway) {
            Log.i(TAG, "Restoring status")
            userManager.setStatus(UserStatus.None)
            autoAway = false
        }
    }
}
