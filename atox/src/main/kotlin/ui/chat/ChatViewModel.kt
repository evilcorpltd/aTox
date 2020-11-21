package ltd.evilcorp.atox.ui.chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FileTransfer
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.domain.feature.ChatManager
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.feature.FileTransferManager
import ltd.evilcorp.domain.tox.PublicKey

private const val TAG = "ChatViewModel"

class ChatViewModel @Inject constructor(
    private val chatManager: ChatManager,
    private val contactManager: ContactManager,
    private val fileTransferManager: FileTransferManager,
    private val notificationHelper: NotificationHelper
) : ViewModel(), CoroutineScope by GlobalScope {
    private var publicKey = PublicKey("")
    private var sentTyping = false

    val contact: LiveData<Contact> by lazy { contactManager.get(publicKey).asLiveData() }
    val messages: LiveData<List<Message>> by lazy { chatManager.messagesFor(publicKey).asLiveData() }
    val fileTransfers: LiveData<List<FileTransfer>> by lazy { fileTransferManager.transfersFor(publicKey).asLiveData() }

    var contactOnline = false

    fun send(message: String, type: MessageType) {
        if (contactOnline) {
            chatManager.sendMessage(publicKey, message, type)
        } else {
            chatManager.queueMessage(publicKey, message, type)
        }
    }

    fun clearHistory() = chatManager.clearHistory(publicKey)
    fun setActiveChat(pubKey: PublicKey) {
        if (pubKey.string().isEmpty()) {
            Log.i(TAG, "Clearing active chat")
            setTyping(false)
        } else {
            Log.i(TAG, "Setting active chat ${pubKey.string().take(8)}")
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

    fun acceptFt(id: Int, destination: Uri) = launch {
        fileTransferManager.accept(id, destination)
    }

    fun rejectFt(id: Int) = launch {
        fileTransferManager.reject(id)
    }

    fun createFt(file: Uri) = launch {
        fileTransferManager.create(publicKey, file)
    }

    fun delete(msg: Message) = launch {
        if (msg.type == MessageType.FileTransfer) {
            fileTransferManager.delete(msg.correlationId)
        }
        chatManager.deleteMessage(msg.id)
    }
}
