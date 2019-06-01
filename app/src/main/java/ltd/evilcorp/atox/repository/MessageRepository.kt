package ltd.evilcorp.atox.repository

import androidx.lifecycle.LiveData
import ltd.evilcorp.atox.db.MessageDao
import ltd.evilcorp.atox.vo.Message
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao
) {
    fun add(message: Message) {
        messageDao.save(message)
    }

    fun get(conversation: ByteArray): LiveData<List<Message>> {
        return messageDao.load(conversation)
    }
}
