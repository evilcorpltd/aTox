package ltd.evilcorp.core.repository

import androidx.lifecycle.LiveData
import ltd.evilcorp.core.db.ContactDao
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.UserStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject internal constructor(
    private val dao: ContactDao
) {
    fun exists(publicKey: String): Boolean = dao.exists(publicKey)
    fun add(contact: Contact) = dao.save(contact)
    fun update(contact: Contact) = dao.update(contact)
    fun delete(contact: Contact) = dao.delete(contact)
    fun get(publicKey: String): LiveData<Contact> = dao.load(publicKey)
    fun getAll(): LiveData<List<Contact>> = dao.loadAll()
    fun resetTransientData() = dao.resetTransientData()

    fun setName(publicKey: String, name: String) = dao.setName(publicKey, name)
    fun setStatusMessage(publicKey: String, statusMessage: String) = dao.setStatusMessage(publicKey, statusMessage)
    fun setLastMessage(publicKey: String, lastMessage: String) = dao.setLastMessage(publicKey, lastMessage)
    fun setUserStatus(publicKey: String, status: UserStatus) = dao.setUserStatus(publicKey, status)
    fun setConnectionStatus(publicKey: String, status: ConnectionStatus) = dao.setConnectionStatus(publicKey, status)
    fun setTyping(publicKey: String, typing: Boolean) = dao.setTyping(publicKey, typing)
    fun setAvatarUri(publicKey: String, uri: String) = dao.setAvatarUri(publicKey, uri)
}
