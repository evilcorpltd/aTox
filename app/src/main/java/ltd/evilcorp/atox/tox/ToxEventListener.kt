package ltd.evilcorp.atox.tox

import android.content.Context
import android.util.Log
import im.tox.tox4j.core.callbacks.ToxCoreEventListener
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxFileControl
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.core.enums.ToxUserStatus
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.repository.*
import ltd.evilcorp.core.vo.*
import java.io.File
import java.io.RandomAccessFile
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "ToxEventListener"

class ToxEventListener @Inject constructor(
    private val context: Context,
    private val contactRepository: ContactRepository,
    private val fileTransferRepository: FileTransferRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val notificationHelper: NotificationHelper,
    private val tox: ToxThread
) : ToxCoreEventListener<Unit> {
    private var contacts: List<Contact> = listOf()
    var contactMapping: List<Pair<String, Int>> = listOf()

    private val fileTransfers: MutableList<FileTransfer> = mutableListOf()

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
        fileTransfers.find { it.publicKey == contact.publicKey && it.fileNumber == fileNumber }?.let { fileTransfer ->
            val avatarFolder = File(context.filesDir, "avatar")
            RandomAccessFile(File(avatarFolder, fileTransfer.fileName), "rwd").apply {
                seek(position)
                write(data)
                close()
            }
        } ?: Log.e(TAG, "Got chunk for file transfer $fileNumber for ${contact.publicKey} we don't know about")
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
        val fileTransfer = FileTransfer(
            contact.publicKey,
            fileNumber,
            kind,
            fileSize,
            if (kind == FileKind.Avatar.ordinal) contact.publicKey else String(filename),
            outgoing = false
        )

        if (fileTransfer.fileKind != FileKind.Avatar.ordinal) {
            App.toxThread.stopFileTransfer(contact.publicKey, fileNumber)
            Log.e(TAG, "Ignored non-avatar file transfer $fileNumber from ${contact.publicKey}")
            return
        }

        fileTransfers.add(fileTransfer)
        fileTransferRepository.add(fileTransfer)

        val avatarFolder = File(context.filesDir, "avatar")
        if (!avatarFolder.exists()) {
            avatarFolder.mkdir()
        }

        RandomAccessFile(File(avatarFolder, fileTransfer.fileName), "rwd").apply {
            setLength(fileSize)
            close()
        }

        when (kind) {
            FileKind.Data.ordinal -> {
                // TODO(robinlinden): Add a chat message allowing the user to accept/reject the transfer.
            }
            FileKind.Avatar.ordinal -> {
                // TODO(robinlinden): Get file ID from Tox and cancel transfer if we already have the file.
                App.toxThread.startFileTransfer(fileTransfer.publicKey, fileNumber)
                contactRepository.update(contact.apply {
                    avatarUri = File(avatarFolder, fileTransfer.fileName).toURI().toString()
                })
            }
            else -> {
                Log.e(TAG, "Got unknown file kind $kind in file transfer")
            }
        }

        Log.e(
            TAG,
            "fileRecv $fileNumber $kind size: $fileSize suggested name: ${String(filename)} from ${contact.publicKey}"
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
