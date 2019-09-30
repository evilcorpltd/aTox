package ltd.evilcorp.atox.feature

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.tox.PublicKey
import ltd.evilcorp.atox.tox.Tox
import ltd.evilcorp.atox.tox.ToxID
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.vo.Contact
import javax.inject.Inject

class ContactManager @Inject constructor(
    private val contactRepository: ContactRepository,
    private val tox: Tox
) : CoroutineScope by GlobalScope {
    fun get(publicKey: PublicKey) = contactRepository.get(publicKey.string())
    fun getAll() = contactRepository.getAll()

    fun add(toxID: ToxID, message: String) = launch {
        tox.addContact(toxID, message)
        contactRepository.add(Contact(toxID.toPublicKey().string()))
    }

    fun delete(publicKey: PublicKey) = launch {
        tox.deleteContact(publicKey)
        contactRepository.delete(Contact(publicKey.string()))
    }
}
