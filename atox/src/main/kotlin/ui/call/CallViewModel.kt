package ltd.evilcorp.atox.ui.call

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.feature.CallManager
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.PublicKey

class CallViewModel @Inject constructor(
    private val callManager: CallManager,
    private val notificationHelper: NotificationHelper,
    private val contactManager: ContactManager,
) : ViewModel(), CoroutineScope by GlobalScope {
    private var publicKey = PublicKey("")

    val contact: LiveData<Contact> by lazy {
        contactManager.get(publicKey).asLiveData()
    }

    fun setActiveContact(pk: PublicKey) {
        publicKey = pk
    }

    fun startCall(): Boolean {
        if (callManager.startCall(publicKey)) {
            launch { notificationHelper.showCallNotification(contactManager.get(publicKey).first()) }
            return true
        }

        return false
    }

    fun endCall() = launch {
        callManager.endCall(publicKey)
        notificationHelper.dismissCallNotification(contactManager.get(publicKey).first())
    }

    val inCall = callManager.inCall
}
