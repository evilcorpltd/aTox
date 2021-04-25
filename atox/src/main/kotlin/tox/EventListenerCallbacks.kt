package ltd.evilcorp.atox.tox

import android.content.Context
import android.util.Log
import im.tox.tox4j.av.enums.ToxavFriendCallState
import im.tox.tox4j.core.enums.ToxFileControl
import java.net.URLConnection
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.settings.FtAutoAccept
import ltd.evilcorp.atox.settings.Settings
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FriendRequestRepository
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FileKind
import ltd.evilcorp.core.vo.FileTransfer
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.Sender
import ltd.evilcorp.domain.av.AudioPlayer
import ltd.evilcorp.domain.feature.ChatManager
import ltd.evilcorp.domain.feature.FileTransferManager
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxAvEventListener
import ltd.evilcorp.domain.tox.ToxEventListener
import ltd.evilcorp.domain.tox.toMessageType

private const val TAG = "EventListenerCallbacks"

private fun isImage(filename: String) = try {
    URLConnection.guessContentTypeFromName(filename).startsWith("image/")
} catch (e: Exception) {
    Log.e(TAG, e.toString())
    false
}

@Singleton
class EventListenerCallbacks @Inject constructor(
    private val ctx: Context,
    private val contactRepository: ContactRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val chatManager: ChatManager,
    private val fileTransferManager: FileTransferManager,
    private val notificationHelper: NotificationHelper,
    private val tox: Tox,
    private val settings: Settings,
) : CoroutineScope by GlobalScope {
    private var contacts: List<Contact> = listOf()
    private var audioPlayer: AudioPlayer? = null

    init {
        launch {
            contactRepository.getAll().collect {
                contacts = it
            }
        }
    }

    private fun contactByPublicKey(publicKey: String) =
        contacts.find { it.publicKey == publicKey }!!

    fun setUp(listener: ToxEventListener) = with(listener) {
        friendStatusMessageHandler = { publicKey, message ->
            contactRepository.setStatusMessage(publicKey, message)
        }

        friendReadReceiptHandler = { publicKey, messageId ->
            messageRepository.setReceipt(publicKey, messageId, Date().time)
        }

        friendStatusHandler = { publicKey, status ->
            contactRepository.setUserStatus(publicKey, status)
        }

        friendConnectionStatusHandler = { publicKey, status ->
            contactRepository.setConnectionStatus(publicKey, status)
            if (status != ConnectionStatus.None) {
                launch {
                    val pending = messageRepository.getPending(publicKey)
                    if (pending.isNotEmpty()) {
                        chatManager.resend(pending)
                    }
                }
            } else {
                fileTransferManager.resetForContact(publicKey)
            }
        }

        friendRequestHandler = { publicKey, _, message ->
            FriendRequest(publicKey, message).also {
                friendRequestRepository.add(it)
                notificationHelper.showFriendRequestNotification(it)
            }
        }

        friendMessageHandler = { publicKey, type, _, msg ->
            messageRepository.add(
                Message(publicKey, msg, Sender.Received, type.toMessageType(), Int.MIN_VALUE, Date().time)
            )

            if (chatManager.activeChat != publicKey) {
                notificationHelper.showMessageNotification(contactByPublicKey(publicKey), msg)
                contactRepository.setHasUnreadMessages(publicKey, true)
            }
        }

        friendNameHandler = { publicKey, newName ->
            contactRepository.setName(publicKey, newName)
        }

        fileRecvChunkHandler = { publicKey, fileNumber, position, data ->
            fileTransferManager.addDataToTransfer(publicKey, fileNumber, position, data)
        }

        fileRecvHandler = { publicKey, fileNo, kind, fileSize, filename ->
            val name = if (kind == FileKind.Avatar.ordinal) publicKey else filename

            val id = fileTransferManager.add(FileTransfer(publicKey, fileNo, kind, fileSize, name, outgoing = false))

            if (kind == FileKind.Data.ordinal) {
                if (chatManager.activeChat != publicKey) {
                    val msg = ctx.getString(R.string.notification_file_transfer, name)
                    notificationHelper.showMessageNotification(contactByPublicKey(publicKey), msg)
                    contactRepository.setHasUnreadMessages(publicKey, true)
                }

                val autoAccept = settings.ftAutoAccept
                if (autoAccept == FtAutoAccept.All || autoAccept == FtAutoAccept.Images && isImage(filename)) {
                    fileTransferManager.accept(id)
                }
            }
        }

        fileRecvControlHandler = { publicKey: String, fileNo: Int, control: ToxFileControl ->
            fileTransferManager.setStatus(publicKey, fileNo, control)
        }

        fileChunkRequestHandler = { publicKey: String, fileNo: Int, position: Long, length: Int ->
            fileTransferManager.sendChunk(publicKey, fileNo, position, length)
        }

        selfConnectionStatusHandler = { status ->
            userRepository.updateConnection(tox.publicKey.string(), status)
        }

        friendTypingHandler = { publicKey, isTyping ->
            contactRepository.setTyping(publicKey, isTyping)
        }
    }

    fun setUp(listener: ToxAvEventListener) = with(listener) {
        callHandler = { pk, audioEnabled, videoEnabled ->
            Log.e(TAG, "call ${pk.take(8)} $audioEnabled $videoEnabled")
            tox.endCall(PublicKey(pk))
        }

        callStateHandler = { pk, callState ->
            Log.e(TAG, "callState ${pk.take(8)} $callState")
            if (callState.contains(ToxavFriendCallState.FINISHED) || callState.contains(ToxavFriendCallState.ERROR)) {
                audioPlayer?.stop()
                audioPlayer = null
            }
        }

        videoBitRateHandler = { pk, bitRate ->
            Log.e(TAG, "videoBitRate ${pk.take(8)} $bitRate")
        }

        videoReceiveFrameHandler = { pk,
            width, height,
            y, u, v,
            yStride, uStride, vStride ->
            Log.v(
                TAG,
                "videoReceiveFrame ${pk.take(8)}" +
                    "$width $height" +
                    "${y.size} ${u.size} ${v.size}" +
                    "$yStride $uStride $vStride"
            )
        }

        audioReceiveFrameHandler = { pk, pcm, channels, samplingRate ->
            Log.v(TAG, "audioBitRate ${pk.take(8)} ${pcm.size} $channels $samplingRate")
        }

        audioBitRateHandler = { pk, bitRate ->
            Log.e(TAG, "audioBitRate ${pk.take(8)} $bitRate")
        }

        audioReceiveFrameHandler = { _, pcm, channels, samplingRate ->
            if (audioPlayer == null) {
                audioPlayer = AudioPlayer(samplingRate, channels)
                audioPlayer?.start()
            }
            audioPlayer?.buffer(pcm)
        }
    }
}
