package ltd.evilcorp.atox.tox

import android.util.Log
import im.tox.tox4j.core.callbacks.ToxCoreEventListener
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxFileControl
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.core.enums.ToxUserStatus
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FriendRequestRepository
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.Sender
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "ToxEventListener"

class ToxEventListener @Inject constructor(
    private val contactRepository: ContactRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val notificationHelper: NotificationHelper
) : ToxCoreEventListener<Int> {
    private var contacts: List<Contact> = listOf()
    var contactMapping: List<Pair<String, Int>> = listOf()

    init {
        contactRepository.getAll().observeForever {
            contacts = it
        }
    }

    private fun contactByFriendNumber(friendNumber: Int): Contact {
        val (publicKey, _) = contactMapping.find { it.second == friendNumber }!!
        return contacts.find { it.publicKey == publicKey }!!
    }

    override fun friendLosslessPacket(friendNumber: Int, data: ByteArray, state: Int?): Int {
        return Log.e(TAG, "friendLosslessPacket")
    }

    override fun fileRecvControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl, state: Int?): Int {
        return Log.e(TAG, "fileRecvControl")
    }

    override fun friendStatusMessage(friendNumber: Int, message: ByteArray, state: Int?): Int {
        contactByFriendNumber(friendNumber).let {
            contactRepository.update(it.apply { statusMessage = String(message) })
        }

        return Log.e(TAG, "friendStatusMessage")
    }

    override fun friendReadReceipt(friendNumber: Int, messageId: Int, state: Int?): Int {
        return Log.e(TAG, "friendReadReceipt")
    }

    override fun friendStatus(friendNumber: Int, toxStatus: ToxUserStatus, state: Int?): Int {
        contactByFriendNumber(friendNumber).let {
            contactRepository.update(it.apply { status = toxStatus.toUserStatus() })
        }

        return Log.e(TAG, "friendStatus")
    }

    override fun friendConnectionStatus(friendNumber: Int, toxConnectionStatus: ToxConnection, state: Int?): Int {
        contactByFriendNumber(friendNumber).let {
            contactRepository.update(it.apply { connectionStatus = toxConnectionStatus.toConnectionStatus() })
        }

        return Log.e(TAG, "friendConnectionStatus")
    }

    override fun friendRequest(publicKey: ByteArray, timeDelta: Int, message: ByteArray, state: Int?): Int {
        FriendRequest(publicKey.bytesToHex(), String(message)).also {
            friendRequestRepository.add(it)
            notificationHelper.showFriendRequestNotification(it)
        }

        return Log.e(TAG, "friendRequest")
    }

    override fun friendMessage(
        friendNumber: Int,
        messageType: ToxMessageType,
        timeDelta: Int,
        message: ByteArray,
        state: Int?
    ): Int {
        contactByFriendNumber(friendNumber).let {
            contactRepository.update(it.apply {
                lastMessage = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date())
            })
            messageRepository.add(Message(it.publicKey, String(message), Sender.Received))

            notificationHelper.showMessageNotification(it, String(message))
        }

        return Log.e(TAG, "friendMessage")
    }

    override fun friendName(friendNumber: Int, newName: ByteArray, state: Int?): Int {
        contactByFriendNumber(friendNumber).let {
            contactRepository.update(it.apply { name = String(newName) })
        }

        return Log.e(TAG, "friendName")
    }

    override fun fileRecvChunk(friendNumber: Int, fileNumber: Int, position: Long, data: ByteArray, state: Int?): Int {
        return Log.e(TAG, "fileRecvChunk")
    }

    override fun fileRecv(
        friendNumber: Int,
        fileNumber: Int,
        kind: Int,
        fileSize: Long,
        filename: ByteArray,
        state: Int?
    ): Int {
        return Log.e(TAG, "fileRecv")
    }

    override fun friendLossyPacket(friendNumber: Int, data: ByteArray, state: Int?): Int {
        return Log.e(TAG, "friendLossyPacket")
    }

    override fun selfConnectionStatus(connectionStatus: ToxConnection, state: Int?): Int {
        userRepository.updateConnection(App.toxThread.publicKey, connectionStatus.toConnectionStatus())
        return Log.e(TAG, "selfConnectionStatus $connectionStatus")
    }

    override fun friendTyping(friendNumber: Int, isTyping: Boolean, state: Int?): Int {
        contactByFriendNumber(friendNumber).let {
            contactRepository.update(it.apply { typing = isTyping })
        }

        return Log.e(TAG, "friendTyping")
    }

    override fun fileChunkRequest(friendNumber: Int, fileNumber: Int, position: Long, length: Int, state: Int?): Int {
        return Log.e(TAG, "fileChunkRequest")
    }
}
