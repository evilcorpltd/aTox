package ltd.evilcorp.atox.repository

import androidx.lifecycle.LiveData
import ltd.evilcorp.atox.db.ContactDao
import ltd.evilcorp.atox.vo.Contact
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val contactDao: ContactDao
) {
    fun exists(publicKey: ByteArray): Boolean {
        return contactDao.exists(publicKey)
    }

    fun addContact(contact: Contact) {
        contactDao.save(contact)
    }

    fun updateContact(contact: Contact) {
        contactDao.update(contact)
    }

    fun deleteContact(contact: Contact) {
        contactDao.delete(contact)
    }

    fun getContact(publicKey: ByteArray): LiveData<Contact> {
        return contactDao.load(publicKey)
    }

    fun getContact(friendNumber: Int): LiveData<Contact> {
        return contactDao.load(friendNumber)
    }

    fun getContacts(): LiveData<List<Contact>> {
        return contactDao.loadAll()
    }
}
