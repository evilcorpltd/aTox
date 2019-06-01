package ltd.evilcorp.atox.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ltd.evilcorp.atox.vo.Message

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(message: Message)

    @Query("SELECT * FROM messages where conversation == :conversation")
    fun load(conversation: ByteArray): LiveData<List<Message>>
}
