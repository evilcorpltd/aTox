package ltd.evilcorp.domain.av

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED
import android.media.AudioManager.EXTRA_SCO_AUDIO_STATE
import android.media.AudioManager.SCO_AUDIO_STATE_CONNECTED
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HeadsetPlugReceiver : BroadcastReceiver() {

    private companion object {
        private const val logTag = "HeadsetPlugReceiver"
        private const val headsetState = "state"
    }

    private val _isPlugged = MutableStateFlow(false)
    val isPlugged: StateFlow<Boolean> = _isPlugged.asStateFlow()


    private fun checkStateOff(intent: Intent) {
        Log.d(logTag, "[HeadsetPlugReceiver.checkStateOff]")
    }

    private fun sendEvent(intent: Intent) {
        val isPlugged = when (intent.action) {
            Intent.ACTION_HEADSET_PLUG -> intent.getIntExtra(headsetState, 0) == 1
            ACTION_SCO_AUDIO_STATE_UPDATED -> intent.getIntExtra(EXTRA_SCO_AUDIO_STATE, 0) == SCO_AUDIO_STATE_CONNECTED
            else -> false
        }
        Log.d(logTag, "[HeadsetPlugReceiver.sendEvent] isPlugged: $isPlugged")
        _isPlugged.value = isPlugged
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(logTag, "[HeadsetPlugReceiver.onReceive] action:  $action")
        when (action) {
            Intent.ACTION_HEADSET_PLUG, ACTION_SCO_AUDIO_STATE_UPDATED -> sendEvent(intent)
            else -> checkStateOff(intent)
        }
    }
}
