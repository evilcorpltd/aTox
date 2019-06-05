package ltd.evilcorp.atox.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.repository.ContactRepository
import ltd.evilcorp.atox.repository.MessageRepository
import ltd.evilcorp.atox.tox.MsgSendMessage
import ltd.evilcorp.atox.tox.ToxThread
import ltd.evilcorp.atox.tox.byteArrayToHex
import ltd.evilcorp.atox.vo.Contact
import ltd.evilcorp.atox.vo.Message
import ltd.evilcorp.atox.vo.Sender
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {
    lateinit var publicKey: ByteArray

    val contact: LiveData<Contact> by lazy { contactRepository.get(publicKey) }
    val messages: LiveData<List<Message>> by lazy { messageRepository.get(publicKey) }

    fun sendMessage(message: String) {
        with(App.toxThread.handler) {
            sendMessage(obtainMessage(ToxThread.msgSendMsg, MsgSendMessage(publicKey.byteArrayToHex(), message)))
        }

        GlobalScope.launch {
            messageRepository.add(
                Message(publicKey, message, Sender.Sent)
            )
        }
    }
}
