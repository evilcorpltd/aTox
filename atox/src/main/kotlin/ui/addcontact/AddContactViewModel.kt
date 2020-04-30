package ltd.evilcorp.atox.ui.addcontact

import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxID
import javax.inject.Inject

class AddContactViewModel @Inject constructor(
    private val contactManager: ContactManager,
    private val tox: Tox,
    private val toxStarter: ToxStarter
) : ViewModel() {
    val contacts = contactManager.getAll()

    fun isToxRunning() = tox.started
    fun tryLoadTox(): Boolean = toxStarter.tryLoadTox()

    fun addContact(toxId: ToxID, message: String) = contactManager.add(toxId, message)
}
