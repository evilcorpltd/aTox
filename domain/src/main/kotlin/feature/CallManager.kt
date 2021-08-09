// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.feature

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.core.content.ContextCompat
import im.tox.tox4j.av.exceptions.ToxavCallControlException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ltd.evilcorp.domain.av.AudioCapture
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox

sealed class CallState {
    object NotInCall : CallState()
    data class InCall(val publicKey: PublicKey) : CallState()
}

private const val TAG = "CallManager"

@Singleton
class CallManager @Inject constructor(
    private val tox: Tox,
    context: Context,
) : CoroutineScope by GlobalScope {
    private val _inCall = MutableStateFlow<CallState>(CallState.NotInCall)
    val inCall: StateFlow<CallState> get() = _inCall

    private val _sendingAudio = MutableStateFlow(false)
    val sendingAudio: StateFlow<Boolean> get() = _sendingAudio

    private val audioManager = ContextCompat.getSystemService(context, AudioManager::class.java)

    fun startCall(publicKey: PublicKey) {
        tox.startCall(publicKey)
        _inCall.value = CallState.InCall(publicKey)
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    fun answerCall(publicKey: PublicKey) {
        tox.answerCall(publicKey)
        _inCall.value = CallState.InCall(publicKey)
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    fun endCall(publicKey: PublicKey) {
        audioManager?.mode = AudioManager.MODE_NORMAL
        _inCall.value = CallState.NotInCall
        try {
            tox.endCall(publicKey)
        } catch (e: ToxavCallControlException) {
            if (e.code() != ToxavCallControlException.Code.FRIEND_NOT_IN_CALL) {
                throw e
            }
        }
    }

    fun startSendingAudio(): Boolean {
        val to = (inCall.value as CallState.InCall?)?.publicKey ?: return false

        val recorder = AudioCapture(48_000, 1)
        if (!recorder.isOk()) {
            return false
        }

        startAudioSender(recorder, to)
        return true
    }

    fun stopSendingAudio() {
        _sendingAudio.value = false
    }

    private fun startAudioSender(recorder: AudioCapture, to: PublicKey) {
        launch {
            recorder.start()
            _sendingAudio.value = true
            while (inCall.value is CallState.InCall && sendingAudio.value) {
                val start = System.currentTimeMillis()
                val audioFrame = recorder.read()
                try {
                    tox.sendAudio(to, audioFrame, 1, 48_000)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }
                val elapsed = System.currentTimeMillis() - start
                if (elapsed < 20) {
                    delay(20 - elapsed)
                }
            }
            recorder.stop()
            recorder.release()
            _sendingAudio.value = false
        }
    }
}
