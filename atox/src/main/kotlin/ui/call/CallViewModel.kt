// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.call

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.feature.CallManager
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.PublicKey
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class CallViewModel(app: App) : AndroidViewModel(app), DIAware {
    override val di by closestDI()

    private val scope: CoroutineScope by instance()
    private val callManager: CallManager by instance()
    private val notificationHelper: NotificationHelper by instance()
    private val contactManager: ContactManager by instance()

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

    val inCall = callManager.inCall
    val sendingAudio = callManager.sendingAudio

    var speakerphoneOn by callManager::speakerphoneOn
}
