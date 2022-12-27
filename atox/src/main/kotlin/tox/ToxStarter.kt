// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.tox

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import im.tox.tox4j.core.exceptions.ToxNewException
import im.tox.tox4j.crypto.exceptions.ToxDecryptionException
import javax.inject.Inject
import ltd.evilcorp.atox.ToxService
import ltd.evilcorp.atox.settings.Settings
import ltd.evilcorp.domain.feature.FileTransferManager
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.SaveManager
import ltd.evilcorp.domain.tox.SaveOptions
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxAvEventListener
import ltd.evilcorp.domain.tox.ToxEventListener
import ltd.evilcorp.domain.tox.ToxSaveStatus
import ltd.evilcorp.domain.tox.testToxSave

private const val TAG = "ToxStarter"

class ToxStarter @Inject constructor(
    private val fileTransferManager: FileTransferManager,
    private val saveManager: SaveManager,
    private val userManager: UserManager,
    private val listenerCallbacks: EventListenerCallbacks,
    private val tox: Tox,
    private val eventListener: ToxEventListener,
    private val avEventListener: ToxAvEventListener,
    private val context: Context,
    private val settings: Settings,
) {
    fun startTox(save: ByteArray? = null, password: String? = null): ToxSaveStatus {
        listenerCallbacks.setUp(eventListener)
        listenerCallbacks.setUp(avEventListener)
        val options =
            SaveOptions(save, settings.udpEnabled, settings.proxyType, settings.proxyAddress, settings.proxyPort)
        try {
            tox.isBootstrapNeeded = true
            tox.start(options, password, eventListener, avEventListener)
        } catch (e: ToxNewException) {
            Log.e(TAG, e.message)
            return testToxSave(options, password)
        } catch (e: ToxDecryptionException) {
            Log.e(TAG, e.message)
            return ToxSaveStatus.Encrypted
        }

        // This can stay alive across core restarts and it doesn't work well when toxcore resets its numbers
        fileTransferManager.reset()
        startService()
        return ToxSaveStatus.Ok
    }

    fun stopTox() = context.run {
        stopService(Intent(this, ToxService::class.java))
    }

    fun tryLoadTox(password: String?): ToxSaveStatus {
        val save = tryLoadSave() ?: return ToxSaveStatus.SaveNotFound
        val status = startTox(save, password)
        if (status == ToxSaveStatus.Ok) {
            userManager.verifyExists(tox.publicKey)
        }
        return status
    }

    private fun startService() = context.run {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            startService(Intent(this, ToxService::class.java))
        } else {
            startForegroundService(Intent(this, ToxService::class.java))
        }
    }

    private fun tryLoadSave(): ByteArray? =
        saveManager.run { list().firstOrNull()?.let { load(PublicKey(it)) } }
}
