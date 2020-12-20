package ltd.evilcorp.atox.ui.settings

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.BootReceiver
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.domain.tox.ProxyType
import ltd.evilcorp.domain.tox.SaveOptions
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxSaveStatus
import ltd.evilcorp.domain.tox.testToxSave

enum class ProxyStatus {
    Good,
    BadPort,
    BadHost,
    BadType,
    NotFound,
}

class SettingsViewModel @Inject constructor(
    private val context: Context,
    private val preferences: SharedPreferences,
    private val toxStarter: ToxStarter,
    private val tox: Tox
) : ViewModel() {
    private var restartNeeded = false

    private val _proxyStatus = MutableLiveData<ProxyStatus>()
    val proxyStatus: LiveData<ProxyStatus> get() = _proxyStatus

    fun getUdpEnabled(): Boolean = preferences.getBoolean("udp_enabled", false)
    fun setUdpEnabled(enabled: Boolean) {
        preferences.edit().putBoolean("udp_enabled", enabled).apply()
        restartNeeded = true
    }

    fun getRunAtStartup(): Boolean = context.packageManager.getComponentEnabledSetting(
        ComponentName(context, BootReceiver::class.java)
    ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED

    fun setRunAtStartup(enabled: Boolean) {
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, BootReceiver::class.java),
            state,
            PackageManager.DONT_KILL_APP
        )
    }

    fun commit() {
        if (!restartNeeded) return
        toxStarter.stopTox()

        GlobalScope.launch {
            while (tox.started) {
                delay(200)
            }
            toxStarter.tryLoadTox()
        }
    }

    var checkProxyJob: Job? = null
    fun checkProxy() {
        checkProxyJob?.cancel(null)
        checkProxyJob = viewModelScope.launch(Dispatchers.IO) {
            val saveStatus = testToxSave(
                SaveOptions(saveData = null, getUdpEnabled(), getProxyType(), getProxyAddress(), getProxyPort())
            )

            val proxyStatus = when (saveStatus) {
                ToxSaveStatus.BadProxyHost -> ProxyStatus.BadHost
                ToxSaveStatus.BadProxyPort -> ProxyStatus.BadPort
                ToxSaveStatus.BadProxyType -> ProxyStatus.BadType
                ToxSaveStatus.ProxyNotFound -> ProxyStatus.NotFound
                else -> ProxyStatus.Good
            }

            _proxyStatus.postValue(proxyStatus)
        }
    }

    fun getProxyType(): ProxyType = ProxyType.values()[preferences.getInt("proxy_type", 0)]
    fun setProxyType(proxyType: ProxyType) {
        if (proxyType != getProxyType()) {
            preferences.edit { putInt("proxy_type", proxyType.ordinal) }
            restartNeeded = true
            checkProxy()
        }
    }

    fun getProxyAddress(): String = preferences.getString("proxy_address", null) ?: ""
    fun setProxyAddress(address: String) {
        if (address != getProxyAddress()) {
            preferences.edit { putString("proxy_address", address) }
            if (getProxyType() != ProxyType.None) {
                restartNeeded = true
            }
            checkProxy()
        }
    }

    fun getProxyPort(): Int = preferences.getInt("proxy_port", 0)
    fun setProxyPort(port: Int) {
        if (port != getProxyPort()) {
            preferences.edit { putInt("proxy_port", port) }
            if (getProxyType() != ProxyType.None) {
                restartNeeded = true
            }
            checkProxy()
        }
    }
}
