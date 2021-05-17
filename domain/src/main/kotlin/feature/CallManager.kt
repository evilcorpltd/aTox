package ltd.evilcorp.domain.feature

import android.util.Log
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

private const val TAG = "CallManager"

@Singleton
class CallManager @Inject constructor(
    private val tox: Tox,
) : CoroutineScope by GlobalScope {
    private val _inCall = MutableStateFlow(false)
    val inCall: StateFlow<Boolean> get() = _inCall

    fun startCall(publicKey: PublicKey): Boolean {
        val recorder = AudioCapture(48_000, 1)
        if (!recorder.isOk()) {
            return false
        }

        tox.startCall(publicKey)
        _inCall.value = true

        launch {
            recorder.start()
            while (inCall.value) {
                val start = System.currentTimeMillis()
                val audioFrame = recorder.read()
                try {
                    tox.sendAudio(publicKey, audioFrame, 1, 48_000)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }
                val elapsed = System.currentTimeMillis() - start
                if (elapsed < 20) {
                    delay(20 - elapsed)
                }
            }
            recorder.stop()
        }
        return true
    }

    fun endCall(publicKey: PublicKey) {
        _inCall.value = false
        try {
            tox.endCall(publicKey)
        } catch (e: ToxavCallControlException) {
            if (e.code() != ToxavCallControlException.Code.FRIEND_NOT_IN_CALL) {
                throw e
            }
        }
    }
}
