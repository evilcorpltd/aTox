package ltd.evilcorp.core.repository

import androidx.lifecycle.LiveData
import ltd.evilcorp.core.db.MessageDao
import ltd.evilcorp.core.vo.Message
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject internal constructor(
    private val messageDao: MessageDao
) {
    fun add(message: Message) =
        messageDao.save(message)

    fun get(conversation: String): LiveData<List<Message>> =
        messageDao.load(conversation)

    fun delete(conversation: String) =
        messageDao.delete(conversation)

    fun setReceipt(conversation: String, correlationId: Int, timestamp: String) =
        messageDao.setReceipt(conversation, correlationId, timestamp)
}
