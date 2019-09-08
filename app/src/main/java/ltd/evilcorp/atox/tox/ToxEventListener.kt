package ltd.evilcorp.atox.tox

import android.util.Log
import im.tox.tox4j.core.callbacks.ToxCoreEventListener
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxFileControl
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.core.enums.ToxUserStatus
import ltd.evilcorp.atox.feature.FileTransferManager
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

private fun getDate() = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date())

class ToxEventListener @Inject constructor(
    private val contactRepository: ContactRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val notificationHelper: NotificationHelper,
    private val tox: Tox,
    private val fileTransferManager: FileTransferManager
) : ToxCoreEventListener<Unit> {
    private var contacts: List<Contact> = listOf()
    var contactMapping: List<Pair<PublicKey, Int>> = listOf()

    init {
        contactRepository.getAll().observeForever {
            contacts = it
        }
    }

    private fun publicKeyByFriendNumber(friendNumber: Int) =
        contactMapping.find { it.second == friendNumber }!!.first.string()

    private fun contactByFriendNumber(friendNumber: Int): Contact {
        val publicKey = publicKeyByFriendNumber(friendNumber)
        return contacts.find { it.publicKey == publicKey }!!
    }


    override fun friendLosslessPacket(friendNumber: Int, data: ByteArray, state: Unit?) {
        Log.e(TAG, "friendLosslessPacket")
    }

    override fun fileRecvControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl, state: Unit?) {
        Log.e(TAG, "fileRecvControl")
    }

    override fun friendStatusMessage(friendNumber: Int, message: ByteArray, state: Unit?) =
        contactRepository.setStatusMessage(publicKeyByFriendNumber(friendNumber), String(message))

    override fun friendReadReceipt(friendNumber: Int, messageId: Int, state: Unit?) {
        Log.e(TAG, "friendReadReceipt")
    }

    override fun friendStatus(friendNumber: Int, toxStatus: ToxUserStatus, state: Unit?) =
        contactRepository.setUserStatus(publicKeyByFriendNumber(friendNumber), toxStatus.toUserStatus())

    override fun friendConnectionStatus(friendNumber: Int, toxConnectionStatus: ToxConnection, state: Unit?) =
        contactRepository.setConnectionStatus(
            publicKeyByFriendNumber(friendNumber),
            toxConnectionStatus.toConnectionStatus()
        )

    override fun friendRequest(publicKey: ByteArray, timeDelta: Int, message: ByteArray, state: Unit?) {
        FriendRequest(publicKey.bytesToHex(), String(message)).also {
            friendRequestRepository.add(it)
            notificationHelper.showFriendRequestNotification(it)
        }
    }

    override fun friendMessage(
        friendNumber: Int,
        messageType: ToxMessageType,
        timeDelta: Int,
        message: ByteArray,
        state: Unit?
    ) = contactByFriendNumber(friendNumber).let {
        contactRepository.setLastMessage(it.publicKey, getDate())
        messageRepository.add(Message(it.publicKey, String(message), Sender.Received))
        notificationHelper.showMessageNotification(it, String(message))
    }

    override fun friendName(friendNumber: Int, newName: ByteArray, state: Unit?) =
        contactRepository.setName(publicKeyByFriendNumber(friendNumber), String(newName))

    override fun fileRecvChunk(friendNumber: Int, fileNumber: Int, position: Long, data: ByteArray, state: Unit?) =
        fileTransferManager.addDataToTransfer(publicKeyByFriendNumber(friendNumber), fileNumber, position, data)

    override fun fileRecv(
        friendNumber: Int,
        fileNumber: Int,
        kind: Int,
        fileSize: Long,
        filename: ByteArray,
        state: Unit?
    ) = fileTransferManager.add(
        FileTransfer(
            publicKeyByFriendNumber(friendNumber),
            fileNumber,
            kind,
            fileSize,
            if (kind == FileKind.Avatar.ordinal) publicKeyByFriendNumber(friendNumber) else String(filename),
            outgoing = false
        )
    )

    override fun friendLossyPacket(friendNumber: Int, data: ByteArray, state: Unit?) {
        Log.e(TAG, "friendLossyPacket")
    }

    override fun selfConnectionStatus(connectionStatus: ToxConnection, state: Unit?) =
        userRepository.updateConnection(tox.publicKey.string(), connectionStatus.toConnectionStatus())

    override fun friendTyping(friendNumber: Int, isTyping: Boolean, state: Unit?) =
        contactRepository.setTyping(publicKeyByFriendNumber(friendNumber), isTyping)

    override fun fileChunkRequest(friendNumber: Int, fileNumber: Int, position: Long, length: Int, state: Unit?) {
        Log.e(TAG, "fileChunkRequest")
    }
}
