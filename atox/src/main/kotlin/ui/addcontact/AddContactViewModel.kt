package ltd.evilcorp.atox.ui.addcontact

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import javax.inject.Inject
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxID
import ltd.evilcorp.domain.tox.ToxSaveStatus

class AddContactViewModel @Inject constructor(
    private val contactManager: ContactManager,
    private val tox: Tox,
    private val toxStarter: ToxStarter
) : ViewModel() {
    val contacts: LiveData<List<Contact>> = contactManager.getAll().asLiveData()

    fun isToxRunning() = tox.started
    fun tryLoadTox(): Boolean = toxStarter.tryLoadTox() == ToxSaveStatus.Ok

    fun addContact(toxId: ToxID, message: String) = contactManager.add(toxId, message)
}
