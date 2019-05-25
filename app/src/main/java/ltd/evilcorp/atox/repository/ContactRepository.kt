package ltd.evilcorp.atox.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ltd.evilcorp.atox.vo.Contact
import ltd.evilcorp.atox.db.ContactDao
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

    fun getContact(publicKey: ByteArray): LiveData<Contact> {
        val data = MutableLiveData<Contact>()
        data.value = contactDao.load(publicKey)
        return data
    }

    fun getContact(friendNumber: Int): LiveData<Contact> {
        val data = MutableLiveData<Contact>()
        data.value = contactDao.load(friendNumber)
        return data
    }

    fun getContacts(): LiveData<List<Contact>> {
        val data = MutableLiveData<List<Contact>>()
        data.value = contactDao.loadAll()
        return data
    }
}
