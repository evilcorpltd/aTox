package ltd.evilcorp.atox.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.repository.ContactRepository
import ltd.evilcorp.atox.repository.MessageRepository
import ltd.evilcorp.atox.vo.Contact
import ltd.evilcorp.atox.vo.Message

class ChatViewModel(
    publicKey: ByteArray,
    contactRepository: ContactRepository,
    messageRepository: MessageRepository
) : ViewModel() {
    val contact: LiveData<Contact> = contactRepository.getContact(publicKey)
    val messages: LiveData<List<Message>> = messageRepository.get(publicKey)
}
