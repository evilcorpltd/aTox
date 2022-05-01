// SPDX-FileCopyrightText: 2020-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.settings

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import ltd.evilcorp.atox.BootReceiver
import ltd.evilcorp.domain.tox.ProxyType
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.DIContext
import org.kodein.di.instance

enum class FtAutoAccept {
    None,
    Images,
    All,
}

enum class BootstrapNodeSource {
    BuiltIn,
    UserProvided,
}

class Settings(override val di: DI, override val diContext: DIContext<*>) : DIAware {
    private val ctx: Context by instance()
    private val packageManager: PackageManager by instance()
    private val preferences: SharedPreferences by instance()

    var theme: Int
        get() = preferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(theme) {
            preferences.edit { putInt("theme", theme) }
            AppCompatDelegate.setDefaultNightMode(theme)
        }

    var udpEnabled: Boolean
        get() = preferences.getBoolean("udp_enabled", false)
        set(enabled) = preferences.edit().putBoolean("udp_enabled", enabled).apply()

    var runAtStartup: Boolean
        get() = packageManager.getComponentEnabledSetting(
            ComponentName(ctx, BootReceiver::class.java)
        ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        set(runAtStartup) {
            val state = if (runAtStartup) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }

            packageManager.setComponentEnabledSetting(
                ComponentName(ctx, BootReceiver::class.java),
                state,
                PackageManager.DONT_KILL_APP
            )
        }

    var autoAwayEnabled: Boolean
        get() = preferences.getBoolean("auto_away_enabled", false)
        set(enabled) = preferences.edit().putBoolean("auto_away_enabled", enabled).apply()

    var autoAwaySeconds: Long
        get() = preferences.getLong("auto_away_seconds", 180)
        set(seconds) = preferences.edit().putLong("auto_away_seconds", seconds).apply()

    var proxyType: ProxyType
        get() = ProxyType.values()[preferences.getInt("proxy_type", 0)]
        set(type) = preferences.edit { putInt("proxy_type", type.ordinal) }

    var proxyAddress: String
        get() = preferences.getString("proxy_address", null) ?: ""
        set(address) = preferences.edit { putString("proxy_address", address) }

    var proxyPort: Int
        get() = preferences.getInt("proxy_port", 0)
        set(port) = preferences.edit { putInt("proxy_port", port) }

    var ftAutoAccept: FtAutoAccept
        get() = FtAutoAccept.values()[preferences.getInt("ft_auto_accept", 0)]
        set(autoAccept) = preferences.edit { putInt("ft_auto_accept", autoAccept.ordinal) }

    var bootstrapNodeSource: BootstrapNodeSource
        get() = BootstrapNodeSource.values()[preferences.getInt("bootstrap_node_source", 0)]
        set(source) = preferences.edit { putInt("bootstrap_node_source", source.ordinal) }

    var disableScreenshots: Boolean
        get() = preferences.getBoolean("disable_screenshots", false)
        set(disable) = preferences.edit { putBoolean("disable_screenshots", disable) }

    var confirmQuitting: Boolean
        get() = preferences.getBoolean("confirm_quitting", true)
        set(confirm) = preferences.edit { putBoolean("confirm_quitting", confirm) }
}
