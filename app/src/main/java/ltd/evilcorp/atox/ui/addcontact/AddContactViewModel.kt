package ltd.evilcorp.atox.ui.addcontact

import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.tox.ToxThread
import javax.inject.Inject

class AddContactViewModel @Inject constructor(private val tox: ToxThread) : ViewModel() {
    fun addContact(toxId: String, message: String) = tox.addContact(toxId, message)
}
