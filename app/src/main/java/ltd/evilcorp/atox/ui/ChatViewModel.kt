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

class ChatViewModel(
    private val publicKey: ByteArray,
    contactRepository: ContactRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {
    val contact: LiveData<Contact> = contactRepository.getContact(publicKey)
    val messages: LiveData<List<Message>> = messageRepository.get(publicKey)

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
