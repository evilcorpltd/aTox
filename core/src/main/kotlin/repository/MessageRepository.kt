// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.repository

import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.db.MessageDao
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.PublicKey

@Singleton
class MessageRepository @Inject internal constructor(
    private val messageDao: MessageDao,
    private val contactRepository: ContactRepository,
) {
    fun add(message: Message) {
        messageDao.save(message)
        contactRepository.setLastMessage(message.publicKey, Date().time)
    }

    fun get(conversation: PublicKey): Flow<List<Message>> = messageDao.load(conversation)

    fun getPending(conversation: PublicKey): List<Message> = messageDao.loadPending(conversation)

    fun setCorrelationId(id: Long, correlationId: Int) = messageDao.setCorrelationId(id, correlationId)

    fun delete(conversation: PublicKey) = messageDao.delete(conversation)

    fun deleteMessage(id: Long) = messageDao.deleteMessage(id)

    fun setReceipt(conversation: PublicKey, correlationId: Int, timestamp: Long) =
        messageDao.setReceipt(conversation, correlationId, timestamp)
}
