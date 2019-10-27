package ltd.evilcorp.atox.feature

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.tox.PublicKey
import ltd.evilcorp.atox.tox.Tox
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.Sender
import javax.inject.Inject

class ChatManager @Inject constructor(
    private val contactRepository: ContactRepository,
    private val messageRepository: MessageRepository,
    private val tox: Tox
) : CoroutineScope by GlobalScope {
    fun messagesFor(publicKey: PublicKey) = messageRepository.get(publicKey.string())

    fun sendMessage(publicKey: PublicKey, message: String) = launch {
        messageRepository.add(
            Message(
                publicKey.string(),
                message,
                Sender.Sent,
                tox.sendMessage(publicKey, message).await()
            )
        )
    }

    fun clearHistory(publicKey: PublicKey) = launch {
        messageRepository.delete(publicKey.string())
        contactRepository.setLastMessage(publicKey.string(), "Never")
    }
}
