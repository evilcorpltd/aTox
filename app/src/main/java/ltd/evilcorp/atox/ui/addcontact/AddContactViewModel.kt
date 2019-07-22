package ltd.evilcorp.atox.ui.addcontact

import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.App

class AddContactViewModel : ViewModel() {
    fun addContact(toxId: String, message: String) = App.toxThread.addContact(toxId, message)
}
