package ltd.evilcorp.core.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.db.MessageDao
import ltd.evilcorp.core.vo.Message

@Singleton
class MessageRepository @Inject internal constructor(
    private val messageDao: MessageDao
) {
    fun add(message: Message) =
        messageDao.save(message)

    fun get(conversation: String): Flow<List<Message>> =
        messageDao.load(conversation)

    fun getPending(conversation: String): List<Message> =
        messageDao.loadPending(conversation)

    fun setCorrelationId(id: Long, correlationId: Int) =
        messageDao.setCorrelationId(id, correlationId)

    fun delete(conversation: String) =
        messageDao.delete(conversation)

    fun setReceipt(conversation: String, correlationId: Int, timestamp: Long) =
        messageDao.setReceipt(conversation, correlationId, timestamp)
}
