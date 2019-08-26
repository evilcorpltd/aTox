package ltd.evilcorp.atox.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.chat.ChatManager
import ltd.evilcorp.atox.tox.PublicKey
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.Message
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val chatManager: ChatManager
) : ViewModel() {
    var publicKey: PublicKey = PublicKey("")

    val contact: LiveData<Contact> by lazy { contactRepository.get(publicKey.string()) }
    val messages: LiveData<List<Message>> by lazy { chatManager.messagesFor(publicKey) }

    fun sendMessage(message: String) = chatManager.sendMessage(publicKey, message)
    fun clearHistory() = chatManager.clearHistory(publicKey)
}
