// SPDX-FileCopyrightText: 2021-2025 Robin Lindén <dev@robinlinden.eu>
// SPDX-FileCopyrightText: 2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.call

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.ProximityScreenOff
import ltd.evilcorp.atox.tox.EventListenerCallbacks
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.domain.feature.Call
import ltd.evilcorp.domain.feature.CallManager
import ltd.evilcorp.domain.feature.ContactManager

class CallViewModel @Inject constructor(
    private val scope: CoroutineScope,
    private val callManager: CallManager,
    private val notificationHelper: NotificationHelper,
    private val contactManager: ContactManager,
    private val proximityScreenOff: ProximityScreenOff,
) : ViewModel() {
    val vmContext = viewModelScope.coroutineContext
    var storedFrameCount: Int = 0
    @Inject
    lateinit var eventListenerCallbacks: EventListenerCallbacks

    val SCREEN_TIMER_MS = 1000L
    private var publicKey = PublicKey("")

    val contact: LiveData<Contact> by lazy {
        contactManager.get(publicKey).asLiveData()
    }

    fun setActiveContact(pk: PublicKey) {
        publicKey = pk
    }

    fun startCall() {
        callManager.startCall(publicKey)
        scope.launch { notificationHelper.showOngoingCallNotification(contactManager.get(publicKey).first()) }
    }

    fun endCall() = scope.launch {
        callManager.endCall(publicKey)
        notificationHelper.dismissCallNotification(publicKey)
    }

    fun startSendingAudio() = callManager.startSendingAudio()
    fun stopSendingAudio() = callManager.stopSendingAudio()

    fun toggleSpeakerphone() {
        speakerphoneOn = !speakerphoneOn
        if (speakerphoneOn) {
            proximityScreenOff.release()
        } else {
            proximityScreenOff.acquire()
        }
    }

    var micOn =  false
    fun toggleMicrophoneControl() {
        if (micOn) {
            micOn = false
            if (sendingAudio.value) {
                stopSendingAudio()
            }
        } else {
            setMicrophoneOn()
        }
    }

    fun setMicrophoneOn() {
        micOn = true
        if (!sendingAudio.value && call.value.state == Call.State.ANSWERED) {
            startSendingAudio()
        }
    }

    fun presentTime(hours: Long, minutes: Int, seconds: Int, nanoseconds: Int): String {
        var sf: String = when (call.value.data?.inOrOut) {
            Call.InOrOut.INCOMING -> "in  "
            Call.InOrOut.OUTGOING -> "out  "
            else -> ""
        }
        sf += if (hours == 0L) String.format("%02d:%02d", minutes, seconds)
              else String.format("%01d:%02d:%02d", hours, minutes, seconds)

        sf += presentFrameRate()
        return sf
    }

    fun presentFrameRate(): String {
        val new = eventListenerCallbacks.getFrameCount()
        var delta = new  - storedFrameCount
        if (delta < 0)   delta = 0
        storedFrameCount = new
        // normal fps = SCREEN_TIMER_MS / CallManager.AUDIO_SEND_INTERVAL_MS = 50
        val asSymbol = if (delta  > 25) "  +" // audio link is working
                       else "  X" // few or no frames are coming in
        return asSymbol
        //return "  ${delta}fps"
    }

    val call = callManager.call
    val callLiveData = callManager.call.asLiveData(vmContext)
    val sendingAudio = callManager.sendingAudio
    var speakerphoneOn by callManager::speakerphoneOn
}
