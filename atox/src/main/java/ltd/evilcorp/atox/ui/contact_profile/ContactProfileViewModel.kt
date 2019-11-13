package ltd.evilcorp.atox.ui.contact_profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.feature.ContactManager
import ltd.evilcorp.atox.tox.PublicKey
import ltd.evilcorp.core.vo.Contact
import javax.inject.Inject

class ContactProfileViewModel @Inject constructor(
    private val contactManager: ContactManager
) : ViewModel() {
    var publicKey: PublicKey = PublicKey("")
    val contact: LiveData<Contact> by lazy { contactManager.get(publicKey) }
}
