package ltd.evilcorp.atox.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.App
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.Sender
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {
    lateinit var publicKey: String

    val contact: LiveData<Contact> by lazy { contactRepository.get(publicKey) }
    val messages: LiveData<List<Message>> by lazy { messageRepository.get(publicKey) }

    fun sendMessage(message: String) {
        App.toxThread.sendMessage(publicKey, message)

        GlobalScope.launch {
            messageRepository.add(
                Message(publicKey, message, Sender.Sent)
            )
        }
    }

    fun clearHistory() {
        GlobalScope.launch {
            messageRepository.delete(publicKey)
            contact.value?.let { contact ->
                contact.lastMessage = "Never"
                contactRepository.update(contact)
            } ?: Log.e("ChatViewModel", "Failed to update lastMessage")
        }
    }
}
