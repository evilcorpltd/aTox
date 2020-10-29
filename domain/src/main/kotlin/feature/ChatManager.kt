package ltd.evilcorp.domain.feature

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.Sender
import ltd.evilcorp.domain.tox.MAX_MESSAGE_LENGTH
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox

@Singleton
class ChatManager @Inject constructor(
    private val contactRepository: ContactRepository,
    private val messageRepository: MessageRepository,
    private val tox: Tox
) : CoroutineScope by GlobalScope {
    var activeChat = ""
        set(value) {
            field = value
            if (value.isNotEmpty()) launch {
                contactRepository.setHasUnreadMessages(value, false)
            }
        }

    fun messagesFor(publicKey: PublicKey) = messageRepository.get(publicKey.string())

    fun sendMessage(publicKey: PublicKey, message: String, type: MessageType = MessageType.Normal) =
        launch {
            var msg = message

            while (msg.length > MAX_MESSAGE_LENGTH) {
                tox.sendMessage(publicKey, msg.take(MAX_MESSAGE_LENGTH), type).start()
                msg = msg.drop(MAX_MESSAGE_LENGTH)
            }

            messageRepository.add(
                Message(
                    publicKey.string(),
                    message,
                    Sender.Sent,
                    type,
                    tox.sendMessage(publicKey, msg, type).await()
                )
            )
        }

    fun queueMessage(publicKey: PublicKey, message: String, type: MessageType) = launch {
        messageRepository.add(
            Message(publicKey.string(), message, Sender.Sent, type, Int.MIN_VALUE)
        )
    }

    fun resend(messages: List<Message>) = launch {
        for (message in messages) {
            var msg = message.message

            while (msg.length > MAX_MESSAGE_LENGTH) {
                tox.sendMessage(
                    PublicKey(message.publicKey),
                    msg.take(MAX_MESSAGE_LENGTH),
                    message.type
                ).start()
                msg = msg.drop(MAX_MESSAGE_LENGTH)
            }

            messageRepository.setCorrelationId(
                message.id,
                tox.sendMessage(PublicKey(message.publicKey), msg, message.type).await()
            )
        }
    }

    fun deleteMessage(id: Long) = launch {
        messageRepository.deleteMessage(id)
    }

    fun clearHistory(publicKey: PublicKey) = launch {
        messageRepository.delete(publicKey.string())
        contactRepository.setLastMessage(publicKey.string(), 0)
    }

    fun setTyping(publicKey: PublicKey, typing: Boolean) = tox.setTyping(publicKey, typing)
}
