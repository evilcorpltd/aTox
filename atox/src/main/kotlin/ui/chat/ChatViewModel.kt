package ltd.evilcorp.atox.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.domain.feature.ChatManager
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.PublicKey
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val chatManager: ChatManager,
    private val contactManager: ContactManager,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    private var publicKey = PublicKey("")
    private var sentTyping = false

    val contact: LiveData<Contact> by lazy { contactManager.get(publicKey) }
    val messages: LiveData<List<Message>> by lazy { chatManager.messagesFor(publicKey) }

    fun send(message: String, type: MessageType) = chatManager.sendMessage(publicKey, message, type)
    fun queue(message: String, type: MessageType) = chatManager.queueMessage(publicKey, message, type)
    fun clearHistory() = chatManager.clearHistory(publicKey)
    fun setActiveChat(pubKey: PublicKey) {
        if (pubKey.string().isEmpty()) {
            setTyping(false)
        }

        publicKey = pubKey
        notificationHelper.dismissNotifications(publicKey)
        chatManager.activeChat = publicKey.string()
    }

    fun setTyping(typing: Boolean) {
        if (publicKey.string().isEmpty()) return
        if (sentTyping != typing) {
            chatManager.setTyping(publicKey, typing)
            sentTyping = typing
        }
    }
}
