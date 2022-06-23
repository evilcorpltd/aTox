// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.repository

import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.db.MessageDao
import ltd.evilcorp.core.vo.Message
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject internal constructor(
    private val messageDao: MessageDao,
    private val contactRepository: ContactRepository,
) {
    fun add(message: Message) {
        messageDao.save(message)
        contactRepository.setLastMessage(message.publicKey, Date().time)
    }

    fun get(conversation: String): Flow<List<Message>> =
        messageDao.load(conversation)

    fun getPending(conversation: String): List<Message> =
        messageDao.loadPending(conversation)

    fun setCorrelationId(id: Long, correlationId: Int) =
        messageDao.setCorrelationId(id, correlationId)

    fun delete(conversation: String) =
        messageDao.delete(conversation)

    fun deleteMessage(id: Long) =
        messageDao.deleteMessage(id)

    fun setReceipt(conversation: String, correlationId: Int, timestamp: Long) =
        messageDao.setReceipt(conversation, correlationId, timestamp)
}
