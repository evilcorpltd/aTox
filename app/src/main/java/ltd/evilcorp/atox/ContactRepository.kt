package ltd.evilcorp.atox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ContactRepository private constructor() {
    companion object {
        val instance = ContactRepository()
    }

    private val contactsByKey = HashMap<ByteArray, Contact>()
    private val contactList = MutableLiveData<ArrayList<Contact>>()


    fun getContact(publicKey: ByteArray): LiveData<Contact> {
        val data = MutableLiveData<Contact>()
        if (!contactsByKey.containsKey(publicKey)) {
            contactsByKey[publicKey] = Contact(publicKey)
            contactList.value = ArrayList(contactsByKey.values)
        }

        data.value = contactsByKey[publicKey]
        return data
    }

    fun getContacts(): LiveData<List<Contact>> {
        val data = MutableLiveData<List<Contact>>()
        data.value = ArrayList<Contact>(contactsByKey.values)
        return data
    }
}
