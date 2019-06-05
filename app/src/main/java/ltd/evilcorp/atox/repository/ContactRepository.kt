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

    fun add(contact: Contact) {
        contactDao.save(contact)
    }

    fun update(contact: Contact) {
        contactDao.update(contact)
    }

    fun delete(contact: Contact) {
        contactDao.delete(contact)
    }

    fun get(publicKey: ByteArray): LiveData<Contact> {
        return contactDao.load(publicKey)
    }

    fun get(friendNumber: Int): LiveData<Contact> {
        return contactDao.load(friendNumber)
    }

    fun getAll(): LiveData<List<Contact>> {
        return contactDao.loadAll()
    }
}
