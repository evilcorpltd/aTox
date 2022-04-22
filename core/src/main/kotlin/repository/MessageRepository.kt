// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.repository

import java.util.Date
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.db.MessageDao
import ltd.evilcorp.core.vo.Message
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class MessageRepository(override val di: DI) : DIAware {
    private val messageDao: MessageDao by instance()
    private val contactRepository: ContactRepository by instance()

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
