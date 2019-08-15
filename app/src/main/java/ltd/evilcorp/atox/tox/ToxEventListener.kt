package ltd.evilcorp.atox.tox

import android.util.Log
import im.tox.tox4j.core.callbacks.ToxCoreEventListener
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxFileControl
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.core.enums.ToxUserStatus
import ltd.evilcorp.atox.filetransfer.FileTransferManager
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FriendRequestRepository
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.*
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "ToxEventListener"

class ToxEventListener @Inject constructor(
    private val contactRepository: ContactRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val notificationHelper: NotificationHelper,
    private val tox: ToxThread,
    private val fileTransferManager: FileTransferManager
) : ToxCoreEventListener<Unit> {
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

    override fun friendLosslessPacket(friendNumber: Int, data: ByteArray, state: Unit?) {
        Log.e(TAG, "friendLosslessPacket")
    }

    override fun fileRecvControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl, state: Unit?) {
        Log.e(TAG, "fileRecvControl")
    }

    override fun friendStatusMessage(friendNumber: Int, message: ByteArray, state: Unit?) {
        contactByFriendNumber(friendNumber).let {
            contactRepository.update(it.apply { statusMessage = String(message) })
        }

        Log.e(TAG, "friendStatusMessage")
    }

    override fun friendReadReceipt(friendNumber: Int, messageId: Int, state: Unit?) {
        Log.e(TAG, "friendReadReceipt")
    }

    override fun friendStatus(friendNumber: Int, toxStatus: ToxUserStatus, state: Unit?) {
        contactByFriendNumber(friendNumber).let {
            contactRepository.update(it.apply { status = toxStatus.toUserStatus() })
        }

        Log.e(TAG, "friendStatus")
    }

    override fun friendConnectionStatus(friendNumber: Int, toxConnectionStatus: ToxConnection, state: Unit?) {
        contactByFriendNumber(friendNumber).let {
            contactRepository.update(it.apply { connectionStatus = toxConnectionStatus.toConnectionStatus() })
        }

        Log.e(TAG, "friendConnectionStatus")
    }

    override fun friendRequest(publicKey: ByteArray, timeDelta: Int, message: ByteArray, state: Unit?) {
        FriendRequest(publicKey.bytesToHex(), String(message)).also {
            friendRequestRepository.add(it)
            notificationHelper.showFriendRequestNotification(it)
        }

        Log.e(TAG, "friendRequest")
    }

    override fun friendMessage(
        friendNumber: Int,
        messageType: ToxMessageType,
        timeDelta: Int,
        message: ByteArray,
        state: Unit?
    ) {
        contactByFriendNumber(friendNumber).let {
            contactRepository.update(it.apply {
                lastMessage = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date())
            })
            messageRepository.add(Message(it.publicKey, String(message), Sender.Received))

            notificationHelper.showMessageNotification(it, String(message))
        }

        Log.e(TAG, "friendMessage")
    }

    override fun friendName(friendNumber: Int, newName: ByteArray, state: Unit?) {
        contactByFriendNumber(friendNumber).let {
            contactRepository.update(it.apply { name = String(newName) })
        }

        Log.e(TAG, "friendName")
    }

    override fun fileRecvChunk(friendNumber: Int, fileNumber: Int, position: Long, data: ByteArray, state: Unit?) {
        val contact = contactByFriendNumber(friendNumber)
        fileTransferManager.addDataToTransfer(contact.publicKey, fileNumber, position, data)
    }

    override fun fileRecv(
        friendNumber: Int,
        fileNumber: Int,
        kind: Int,
        fileSize: Long,
        filename: ByteArray,
        state: Unit?
    ) {
        val contact = contactByFriendNumber(friendNumber)
        val ft = FileTransfer(
            contact.publicKey,
            fileNumber,
            kind,
            fileSize,
            if (kind == FileKind.Avatar.ordinal) contact.publicKey else String(filename),
            outgoing = false
        )

        when (kind) {
            FileKind.Data.ordinal -> {
                // TODO(robinlinden): Add a chat message allowing the user to accept/reject the transfer.
                Log.e(TAG, "Ignoring non-avatar file transfer $fileNumber (${ft.fileName}) from ${contact.publicKey}")
                fileTransferManager.reject(ft)
            }
            FileKind.Avatar.ordinal -> {
                fileTransferManager.accept(ft)
            }
            else -> {
                Log.e(TAG, "Got unknown file kind $kind in file transfer")
            }
        }

        Log.e(
            TAG,
            "fileRecv $fileNumber $kind size: $fileSize suggested name: ${ft.fileName} from ${contact.publicKey}"
        )
    }

    override fun friendLossyPacket(friendNumber: Int, data: ByteArray, state: Unit?) {
        Log.e(TAG, "friendLossyPacket")
    }

    override fun selfConnectionStatus(connectionStatus: ToxConnection, state: Unit?) {
        userRepository.updateConnection(tox.publicKey, connectionStatus.toConnectionStatus())
        Log.e(TAG, "selfConnectionStatus $connectionStatus")
    }

    override fun friendTyping(friendNumber: Int, isTyping: Boolean, state: Unit?) {
        contactByFriendNumber(friendNumber).let {
            contactRepository.update(it.apply { typing = isTyping })
        }

        Log.e(TAG, "friendTyping")
    }

    override fun fileChunkRequest(friendNumber: Int, fileNumber: Int, position: Long, length: Int, state: Unit?) {
        Log.e(TAG, "fileChunkRequest")
    }
}
