package ltd.evilcorp.atox.ui.addcontact

import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.tox.ToxID
import ltd.evilcorp.atox.tox.Tox
import javax.inject.Inject

class AddContactViewModel @Inject constructor(private val tox: Tox) : ViewModel() {
    fun addContact(toxId: ToxID, message: String) = tox.addContact(toxId, message)
}
