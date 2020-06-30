package ltd.evilcorp.core.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ltd.evilcorp.core.vo.Message

@Dao
internal interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(message: Message)

    @Query("SELECT * FROM messages WHERE conversation == :conversation")
    fun load(conversation: String): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE conversation == :conversation AND timestamp == 0")
    fun loadPending(conversation: String): List<Message>

    @Query("UPDATE messages SET correlation_id = :correlationId WHERE id == :id")
    fun setCorrelationId(id: Long, correlationId: Int)

    @Query("DELETE FROM messages WHERE conversation == :conversation")
    fun delete(conversation: String)

    @Query("UPDATE messages SET timestamp = :timestamp WHERE conversation == :conversation AND correlation_id == :correlationId") // ktlint-disable
    fun setReceipt(conversation: String, correlationId: Int, timestamp: Long)
}
