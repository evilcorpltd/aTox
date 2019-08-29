package ltd.evilcorp.atox.feature

import ltd.evilcorp.atox.tox.PublicKey
import ltd.evilcorp.atox.tox.Tox
import ltd.evilcorp.atox.tox.ToxID
import ltd.evilcorp.core.repository.ContactRepository
import javax.inject.Inject

class ContactManager @Inject constructor(
    private val contactRepository: ContactRepository,
    private val tox: Tox
) {
    fun get(publicKey: PublicKey) = contactRepository.get(publicKey.string())
    fun getAll() = contactRepository.getAll()

    fun add(toxID: ToxID, message: String) = tox.addContact(toxID, message)
    fun delete(publicKey: PublicKey) = tox.deleteContact(publicKey)
}
