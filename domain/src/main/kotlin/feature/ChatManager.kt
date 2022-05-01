// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.feature

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.Sender
import ltd.evilcorp.domain.tox.MAX_MESSAGE_LENGTH
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

private fun String.chunked(chunkSizeInBytes: Int): MutableList<String> {
    val encoder = StandardCharsets.UTF_8.newEncoder()
    val tmp = ByteBuffer.allocate(chunkSizeInBytes - 1)
    val input = CharBuffer.wrap(this)
    val chunks: MutableList<String> = ArrayList()
    var currentIdx = 0

    do {
        val res = encoder.encode(input, tmp, true)
        val nextIdx = this.length - input.length
        chunks.add(this.substring(currentIdx, nextIdx))
        currentIdx = nextIdx
        tmp.rewind()
    } while (res.isOverflow)

    return chunks
}

class ChatManager(override val di: DI) : DIAware {
    private val scope: CoroutineScope by instance()
    private val contactRepository: ContactRepository by instance()
    private val messageRepository: MessageRepository by instance()
    private val tox: Tox by instance()

    var activeChat = ""
        set(value) {
            field = value
            if (value.isNotEmpty()) scope.launch {
                contactRepository.setHasUnreadMessages(value, false)
            }
        }

    fun messagesFor(publicKey: PublicKey) = messageRepository.get(publicKey.string())

    fun sendMessage(publicKey: PublicKey, message: String, type: MessageType = MessageType.Normal) =
        scope.launch {
            if (contactRepository.get(publicKey.string()).first().connectionStatus == ConnectionStatus.None) {
                queueMessage(publicKey, message, type)
                return@launch
            }

            val msgs = message.chunked(MAX_MESSAGE_LENGTH)
            while (msgs.size > 1) {
                tox.sendMessage(publicKey, msgs.removeFirst(), type)
            }

            messageRepository.add(
                Message(
                    publicKey.string(),
                    message,
                    Sender.Sent,
                    type,
                    tox.sendMessage(publicKey, msgs.first(), type),
                )
            )
        }

    private fun queueMessage(publicKey: PublicKey, message: String, type: MessageType) =
        messageRepository.add(Message(publicKey.string(), message, Sender.Sent, type, Int.MIN_VALUE))

    fun resend(messages: List<Message>) = scope.launch {
        for (message in messages) {
            val msgs = message.message.chunked(MAX_MESSAGE_LENGTH)

            while (msgs.size > 1) {
                tox.sendMessage(PublicKey(message.publicKey), msgs.removeFirst(), message.type)
            }

            messageRepository.setCorrelationId(
                message.id,
                tox.sendMessage(PublicKey(message.publicKey), msgs.first(), message.type),
            )
        }
    }

    fun deleteMessage(id: Long) = scope.launch {
        messageRepository.deleteMessage(id)
    }

    fun clearHistory(publicKey: PublicKey) = scope.launch {
        messageRepository.delete(publicKey.string())
        contactRepository.setLastMessage(publicKey.string(), 0)
    }

    fun setTyping(publicKey: PublicKey, typing: Boolean) = tox.setTyping(publicKey, typing)
}
