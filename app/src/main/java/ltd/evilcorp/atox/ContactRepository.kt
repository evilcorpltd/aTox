package ltd.evilcorp.atox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ContactRepository private constructor() {
    companion object {
        val instance = ContactRepository()
    }

    private val contactDao = ContactDatabase.instance().contactDao()

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

    fun getContact(friendNumber: Int) : LiveData<Contact> {
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
