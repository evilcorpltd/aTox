// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
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
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.av.AudioPlayer
import ltd.evilcorp.domain.feature.CallManager
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

private const val FINGERPRINT_LEN = 8
private fun String.fingerprint() = this.take(FINGERPRINT_LEN)

@Singleton
class EventListenerCallbacks @Inject constructor(
    private val ctx: Context,
    private val contactRepository: ContactRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val callManager: CallManager,
    private val chatManager: ChatManager,
    private val fileTransferManager: FileTransferManager,
    private val notificationHelper: NotificationHelper,
    private val tox: Tox,
    private val settings: Settings,
) {
    private var audioPlayer: AudioPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private suspend fun tryGetContact(pk: String, tag: String) =
        contactRepository.get(pk).firstOrNull().let {
            if (it == null) Log.e(TAG, "$tag -> unable to get contact for ${pk.fingerprint()}")
            it
        }

    private fun notifyMessage(contact: Contact, message: String) =
        notificationHelper.showMessageNotification(contact, message, silent = tox.getStatus() == UserStatus.Busy)

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
                scope.launch {
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
                notificationHelper.showFriendRequestNotification(it, silent = tox.getStatus() == UserStatus.Busy)
            }
        }

        friendMessageHandler = { publicKey, type, _, msg ->
            messageRepository.add(
                Message(publicKey, msg, Sender.Received, type.toMessageType(), Int.MIN_VALUE, Date().time)
            )

            if (chatManager.activeChat != publicKey) {
                scope.launch {
                    val contact = tryGetContact(publicKey, "Message") ?: return@launch
                    notifyMessage(contact, msg)
                }
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
                    scope.launch {
                        val contact = tryGetContact(publicKey, "FileRecv") ?: return@launch
                        val msg = ctx.getString(R.string.notification_file_transfer, name)
                        notifyMessage(contact, msg)
                    }
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
            Log.e(TAG, "call ${pk.fingerprint()} $audioEnabled $videoEnabled")
            scope.launch {
                val contact = tryGetContact(pk, "Call") ?: return@launch
                notificationHelper.showPendingCallNotification(tox.getStatus(), contact)
                callManager.addPendingCall(contact)
            }
        }

        callStateHandler = { pk, callState ->
            Log.e(TAG, "callState ${pk.fingerprint()} $callState")
            if (callState.contains(ToxavFriendCallState.FINISHED) || callState.contains(ToxavFriendCallState.ERROR)) {
                audioPlayer?.stop()
                audioPlayer?.release()
                audioPlayer = null
                notificationHelper.dismissCallNotification(PublicKey(pk))
                callManager.endCall(PublicKey(pk))
            }
        }

        videoBitRateHandler = { pk, bitRate ->
            Log.e(TAG, "videoBitRate ${pk.fingerprint()} $bitRate")
        }

        videoReceiveFrameHandler = { pk,
            width, height,
            y, u, v,
            yStride, uStride, vStride ->
            Log.v(
                TAG,
                "videoReceiveFrame ${pk.fingerprint()}" +
                    "$width $height" +
                    "${y.size} ${u.size} ${v.size}" +
                    "$yStride $uStride $vStride"
            )
        }

        audioBitRateHandler = { pk, bitRate ->
            Log.e(TAG, "audioBitRate ${pk.fingerprint()} $bitRate")
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
