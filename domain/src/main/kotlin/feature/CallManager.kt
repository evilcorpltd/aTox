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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.domain.av.AudioCapture
import ltd.evilcorp.domain.tox.Tox

data class Call(public val state: State = State.IDLE, public val data: Data? = null) {
    enum class State { IDLE, CALLING_OUT, PENDING, ANSWERED }
    enum class InOrOut { NA, INCOMING, OUTGOING }
    data class Data(val publicKey: PublicKey, val inOrOut: InOrOut, val startTime: Long)
}

fun Call.setData(aPk: PublicKey, aInOrOut: Call.InOrOut, aSt: Long): Call =
    this.copy(data = Call.Data(aPk, aInOrOut, aSt))
fun Call.setState(newState: Call.State): Call {
    if (newState == Call.State.IDLE) return Call(newState, null)
    return Call(newState, data)
}
fun Call.inCall(): Boolean = state == Call.State.CALLING_OUT || state == Call.State.ANSWERED

private const val TAG = "CallManager"

private const val AUDIO_CHANNELS = 1
private const val AUDIO_SAMPLING_RATE_HZ = 48_000
private const val AUDIO_SEND_INTERVAL_MS = 20

@Singleton
class CallManager @Inject constructor(private val tox: Tox, private val scope: CoroutineScope, context: Context) {
    private val _call = MutableStateFlow<Call>(Call(Call.State.IDLE, null))
    val call: StateFlow<Call> get() = _call

    private val _pendingCalls = MutableStateFlow<MutableSet<Contact>>(mutableSetOf())
    val pendingCalls: StateFlow<Set<Contact>> get() = _pendingCalls

    private val _sendingAudio = MutableStateFlow(false)
    val sendingAudio: StateFlow<Boolean> get() = _sendingAudio

    private val audioManager = ContextCompat.getSystemService(context, AudioManager::class.java)

    private fun toState(newState: Call.State) {
        _call.update { current ->
            current.setState(newState)
        }
    }

    private fun addCallData(newState: Call.State, aPk: PublicKey, aInOrOut: Call.InOrOut, aSt: Long) {
        _call.update { current ->
            current.setState(newState).setData(aPk, aInOrOut, aSt)
        }
    }
    private fun addCallData(newState: Call.State, aPk: PublicKey, aInOrOut: Call.InOrOut) {
        _call.update { current ->
            current.setState(newState).setData(aPk, aInOrOut, SystemClock.elapsedRealtime())
        }
    }

    fun addPendingCall(from: Contact) {
        val calls = mutableSetOf<Contact>().apply { addAll(_pendingCalls.value) }
        calls.addAll(_pendingCalls.value)
        if (calls.add(from)) {
            Log.i(TAG, "Added pending call ${from.publicKey.take(8)}")
            _pendingCalls.value = calls
        }
        if (!_pendingCalls.value.isEmpty()) {
            if (_call.value.state == Call.State.IDLE) {
                toState(Call.State.PENDING)
            } else {
                Log.e(TAG, "Got pending call while state=${_call.value.state}")
            }
        }
    }

    fun removePendingCall(pk: PublicKey) {
        val calls = mutableSetOf<Contact>().apply { addAll(_pendingCalls.value) }
        val removed = calls.firstOrNull { it.publicKey == pk.string() }
        if (removed != null) {
            Log.i(TAG, "Removed pending call ${pk.fingerprint()}")
            calls.remove(removed)
            _pendingCalls.value = calls
        }
        if (_pendingCalls.value.isEmpty() && _call.value.state == Call.State.PENDING) {
            toState(Call.State.IDLE)
        }
    }

    fun startCall(publicKey: PublicKey) {
        val toAnswer = pendingCalls.value.any { it.publicKey == publicKey.string() }
        if (toAnswer) {
            tox.answerCall(publicKey)
        } else {
            tox.startCall(publicKey)
        }
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
        removePendingCall(publicKey)
        if (toAnswer) {
            addCallData(Call.State.ANSWERED, publicKey, Call.InOrOut.INCOMING)
        } else {
            addCallData(Call.State.CALLING_OUT, publicKey, Call.InOrOut.OUTGOING)
        }
    }

    fun endCall(publicKey: PublicKey) {
        if (call.value.inCall() && call.value.data?.publicKey == publicKey) {
            audioManager?.mode = AudioManager.MODE_NORMAL
            // move to below ?
            toState(Call.State.IDLE)
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
        if (!call.value.inCall()) return false
        val to = call.value.data?.publicKey ?: return false
        if (_sendingAudio.value) {
            return true
        }
        val recorder = AudioCapture.create(
            AUDIO_SAMPLING_RATE_HZ,
            AUDIO_CHANNELS,
            AUDIO_SEND_INTERVAL_MS,
        ) ?: return false
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
            while (call.value.inCall() && sendingAudio.value) {
                val start = System.currentTimeMillis()
                val audioFrame = recorder.read()
                try {
                    tox.sendAudio(
                        to,
                        audioFrame,
                        AUDIO_CHANNELS,
                        AUDIO_SAMPLING_RATE_HZ,
                    )
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

    fun setAnswered(pk: PublicKey) {
        if (_call.value.state != Call.State.CALLING_OUT) {
            Log.e(TAG, "Cot answer while in state ${_call.value.state}")
        }
        addCallData(Call.State.ANSWERED, pk, Call.InOrOut.OUTGOING)
    }
}
