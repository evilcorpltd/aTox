package ltd.evilcorp.atox

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// TODO(robinlinden): This should return LiveData<T> everywhere.

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(contact: Contact)

    @Query("SELECT COUNT(*) FROM contact WHERE publicKey = :publicKey")
    fun exists(publicKey: ByteArray): Boolean

    @Query("SELECT * FROM contact WHERE publicKey = :publicKey")
    fun load(publicKey: ByteArray): Contact

    @Query("SELECT * FROM contact WHERE friendNumber = :friendNumber")
    fun load(friendNumber: Int): Contact

    @Query("SELECT * FROM contact")
    fun loadAll(): List<Contact>
}
