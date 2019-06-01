package ltd.evilcorp.atox.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.repository.ContactRepository
import ltd.evilcorp.atox.vo.Contact
import ltd.evilcorp.atox.vo.Message

class ChatViewModel(friendNumber: Int, contactRepository: ContactRepository) : ViewModel() {
    val contact: LiveData<Contact> = contactRepository.getContact(friendNumber)
    val messages = ArrayList<Message>()
}
