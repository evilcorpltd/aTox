// SPDX-FileCopyrightText: 2019-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.settings.BootstrapNodeSource
import ltd.evilcorp.atox.settings.FtAutoAccept
import ltd.evilcorp.atox.settings.Settings
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.domain.tox.BootstrapNodeJsonParser
import ltd.evilcorp.domain.tox.BootstrapNodeRegistry
import ltd.evilcorp.domain.tox.ProxyType
import ltd.evilcorp.domain.tox.SaveOptions
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxSaveStatus
import ltd.evilcorp.domain.tox.testToxSave
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

private const val TOX_SHUTDOWN_POLL_DELAY_MS = 200L

enum class ProxyStatus {
    Good,
    BadPort,
    BadHost,
    BadType,
    NotFound,
}

class SettingsViewModel(app: App) : AndroidViewModel(app), DIAware {
    override val di by closestDI()

    private val filesDir: File by instance(tag = "files")
    private val resolver: ContentResolver by instance()
    private val settings: Settings by instance()
    private val toxStarter: ToxStarter by instance()
    private val tox: Tox by instance()
    private val nodeRegistry: BootstrapNodeRegistry by instance()

    private var restartNeeded = false

    private val _proxyStatus = MutableLiveData<ProxyStatus>()
    val proxyStatus: LiveData<ProxyStatus> get() = _proxyStatus

    private val _committed = MutableLiveData<Boolean>().apply { value = false }
    val committed: LiveData<Boolean> get() = _committed

    fun nospamAvailable(): Boolean = tox.started
    fun getNospam(): Int = tox.nospam
    fun setNospam(value: Int) {
        tox.nospam = value
    }

    // The trickery here is because the values in the dropdown are 0, 1, 2 for auto, no, yes;
    // while in Android, the values are -1, 1, 2 for auto, no, yes; so we map -1 to 0 when getting,
    // and 0 to -1 when setting.
    fun getTheme(): Int = max(0, settings.theme)
    fun setTheme(theme: Int) {
        settings.theme = when (theme) {
            0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    fun getFtAutoAccept(): FtAutoAccept = settings.ftAutoAccept
    fun setFtAutoAccept(autoAccept: FtAutoAccept) {
        settings.ftAutoAccept = autoAccept
    }

    fun getUdpEnabled(): Boolean = settings.udpEnabled
    fun setUdpEnabled(enabled: Boolean) {
        if (enabled == getUdpEnabled()) return
        settings.udpEnabled = enabled
        restartNeeded = true
    }

    fun getRunAtStartup(): Boolean = settings.runAtStartup
    fun setRunAtStartup(enabled: Boolean) {
        settings.runAtStartup = enabled
    }

    fun getAutoAwayEnabled() = settings.autoAwayEnabled
    fun setAutoAwayEnabled(enabled: Boolean) {
        settings.autoAwayEnabled = enabled
    }

    fun getConfirmQuitting(): Boolean = settings.confirmQuitting
    fun setConfirmQuitting(enabled: Boolean) {
        settings.confirmQuitting = enabled
    }

    fun getAutoAwaySeconds() = settings.autoAwaySeconds
    fun setAutoAwaySeconds(seconds: Long) {
        settings.autoAwaySeconds = seconds
    }

    fun commit() {
        if (!restartNeeded) {
            _committed.value = true
            return
        }

        val password = tox.password
        toxStarter.stopTox()

        viewModelScope.launch {
            while (tox.started) {
                delay(TOX_SHUTDOWN_POLL_DELAY_MS)
            }
            toxStarter.tryLoadTox(password)
            _committed.value = true
        }
    }

    private var checkProxyJob: Job? = null
    fun checkProxy() {
        checkProxyJob?.cancel(null)
        checkProxyJob = viewModelScope.launch(Dispatchers.IO) {
            val saveStatus = testToxSave(
                SaveOptions(saveData = null, getUdpEnabled(), getProxyType(), getProxyAddress(), getProxyPort()), null
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

    fun getProxyType(): ProxyType = settings.proxyType
    fun setProxyType(type: ProxyType) {
        if (type != getProxyType()) {
            settings.proxyType = type
            restartNeeded = true
            checkProxy()
        }
    }

    fun getProxyAddress(): String = settings.proxyAddress
    fun setProxyAddress(address: String) {
        if (address != getProxyAddress()) {
            settings.proxyAddress = address
            if (getProxyType() != ProxyType.None) {
                restartNeeded = true
            }
            checkProxy()
        }
    }

    fun getProxyPort(): Int = settings.proxyPort
    fun setProxyPort(port: Int) {
        if (port != getProxyPort()) {
            settings.proxyPort = port
            if (getProxyType() != ProxyType.None) {
                restartNeeded = true
            }
            checkProxy()
        }
    }

    fun isCurrentPassword(maybeCurrentPassword: String) =
        tox.password == maybeCurrentPassword.ifEmpty { null }

    fun setPassword(newPassword: String) =
        tox.changePassword(newPassword.ifEmpty { null })

    fun getBootstrapNodeSource(): BootstrapNodeSource = settings.bootstrapNodeSource
    fun setBootstrapNodeSource(source: BootstrapNodeSource) {
        settings.bootstrapNodeSource = source
        nodeRegistry.reset()
        restartNeeded = true
    }

    suspend fun validateNodeJson(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val bytes = resolver.openInputStream(uri)?.use {
            it.readBytes()
        } ?: return@withContext false

        return@withContext BootstrapNodeJsonParser.parse(bytes.decodeToString()).isNotEmpty()
    }

    suspend fun importNodeJson(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val bytes = resolver.openInputStream(uri)?.use {
            it.readBytes()
        } ?: return@withContext false

        val out = File(filesDir, "user_nodes.json")
        out.delete()
        if (!out.createNewFile()) return@withContext false

        out.outputStream().use { it.write(bytes) }
        return@withContext true
    }

    fun getDisableScreenshots(): Boolean = settings.disableScreenshots
    fun setDisableScreenshots(disable: Boolean) {
        settings.disableScreenshots = disable
    }
}
