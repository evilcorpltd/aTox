// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.call

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.ProximityScreenOff
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.feature.CallManager
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.PublicKey

class CallViewModel @Inject constructor(
    private val scope: CoroutineScope,
    private val callManager: CallManager,
    private val notificationHelper: NotificationHelper,
    private val contactManager: ContactManager,
    private val proximityScreenOff: ProximityScreenOff,
) : ViewModel() {
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

    fun applyProximityScreenSetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (speakerphoneOn) {
                proximityScreenOff.release()
            } else {
                proximityScreenOff.acquire()
            }
        }
    }

    fun disableSpeakerphone() {
        speakerphoneOn = false
        applyProximityScreenSetting()
    }

    fun toggleSpeakerphone() {
        speakerphoneOn = !speakerphoneOn
        applyProximityScreenSetting()
    }

    val inCall = callManager.inCall
    val sendingAudio = callManager.sendingAudio

    var speakerphoneOn by callManager::speakerphoneOn
}
