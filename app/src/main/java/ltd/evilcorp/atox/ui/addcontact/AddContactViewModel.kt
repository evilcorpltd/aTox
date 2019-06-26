package ltd.evilcorp.atox.ui.addcontact

import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.tox.MsgAddContact
import ltd.evilcorp.atox.tox.ToxThread

class AddContactViewModel : ViewModel() {
    fun addContact(toxId: String, message: String) {
        with(App.toxThread.handler) {
            sendMessage(obtainMessage(ToxThread.msgAddContact, MsgAddContact(toxId, message)))
        }
    }
}
