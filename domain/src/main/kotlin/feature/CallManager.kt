// SPDX-FileCopyrightText: 2021-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.feature

import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import im.tox.tox4j.av.exceptions.ToxavCallControlException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.domain.av.AudioCapture
import ltd.evilcorp.domain.tox.Tox

sealed class CallState {
    object NotInCall : CallState()
    data class InCall(val publicKey: PublicKey, val startTime: Long) : CallState()

    companion object {
        const val IDLE = 0
        const val CALLING_OUT = 1
        const val PENDING = 2
        const val ANSWERED = 3
    }
}

private const val TAG = "CallManager"

private const val AUDIO_CHANNELS = 1
private const val AUDIO_SAMPLING_RATE_HZ = 48_000
private const val AUDIO_SEND_INTERVAL_MS = 20

@Singleton
class CallManager @Inject constructor(private val tox: Tox, private val scope: CoroutineScope, context: Context) {
    private val _inCall = MutableStateFlow<CallState>(CallState.NotInCall)
    val inCall: StateFlow<CallState> get() = _inCall

    private val _established = MutableStateFlow<Int>(CallState.IDLE)
    val established : StateFlow<Int> get() = _established

    private val _pendingCalls = MutableStateFlow<MutableSet<Contact>>(mutableSetOf())
    val pendingCalls: StateFlow<Set<Contact>> get() = _pendingCalls

    private val _sendingAudio = MutableStateFlow(false)
    val sendingAudio: StateFlow<Boolean> get() = _sendingAudio

    private val audioManager = ContextCompat.getSystemService(context, AudioManager::class.java)

    fun addPendingCall(from: Contact) {
        val calls = mutableSetOf<Contact>().apply { addAll(_pendingCalls.value) }
        calls.addAll(_pendingCalls.value)
        if (calls.add(from)) {
            Log.i(TAG, "Added pending call ${from.publicKey.take(8)}")
            _pendingCalls.value = calls
        }
        if ((! _pendingCalls.value.isEmpty()) && _established.value == CallState.IDLE)
                _established.value = CallState.PENDING
    }

    fun removePendingCall(pk: PublicKey) {
        val calls = mutableSetOf<Contact>().apply { addAll(_pendingCalls.value) }
        val removed = calls.firstOrNull { it.publicKey == pk.string() }
        if (removed != null) {
            Log.i(TAG, "Removed pending call ${pk.fingerprint()}")
            calls.remove(removed)
            _pendingCalls.value = calls
        }
        if ( _pendingCalls.value.isEmpty() && _established.value == CallState.PENDING)
            _established.value = CallState.IDLE
    }

    fun startCall(publicKey: PublicKey) {
        if (pendingCalls.value.any { it.publicKey == publicKey.string() }) {
            tox.answerCall(publicKey)
            _established.value = CallState.ANSWERED
        } else {
            tox.startCall(publicKey)
            _established.value = CallState.CALLING_OUT
        }
        _inCall.value = CallState.InCall(publicKey, SystemClock.elapsedRealtime())
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
        removePendingCall(publicKey)
    }

    fun endCall(publicKey: PublicKey) {
        val state = inCall.value
        if (state is CallState.InCall && state.publicKey == publicKey) {
            audioManager?.mode = AudioManager.MODE_NORMAL
            _established.value = CallState.IDLE
            _inCall.value = CallState.NotInCall
        }

        removePendingCall(publicKey)

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
        val recorder =
            AudioCapture.create(AUDIO_SAMPLING_RATE_HZ, AUDIO_CHANNELS, AUDIO_SEND_INTERVAL_MS) ?: return false
        startAudioSender(recorder, to)
        return true
    }

    fun stopSendingAudio() {
        _sendingAudio.value = false
    }

    var speakerphoneOn: Boolean
        get() = audioManager?.isSpeakerphoneOn ?: false
        set(value) {
            audioManager?.isSpeakerphoneOn = value
        }

    private fun startAudioSender(recorder: AudioCapture, to: PublicKey) {
        scope.launch {
            recorder.start()
            _sendingAudio.value = true
            while (inCall.value is CallState.InCall && sendingAudio.value) {
                val start = System.currentTimeMillis()
                val audioFrame = recorder.read()
                try {
                    tox.sendAudio(to, audioFrame, AUDIO_CHANNELS, AUDIO_SAMPLING_RATE_HZ)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }
                val elapsed = System.currentTimeMillis() - start
                if (elapsed < AUDIO_SEND_INTERVAL_MS) {
                    delay(AUDIO_SEND_INTERVAL_MS - elapsed)
                }
            }
            recorder.stop()
            recorder.release()
            _sendingAudio.value = false
        }
    }

    fun setAnswered() : Unit {
        if (_established.value != CallState.CALLING_OUT) {
            Log.e(TAG, "Cot answer while in state ${_established.value}")
        }
        _established.value = CallState.ANSWERED
    }
}
