package ltd.evilcorp.atox.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ltd.evilcorp.atox.vo.Contact

// TODO(robinlinden): This should return LiveData<T> everywhere.

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(contact: Contact)

    @Query("SELECT COUNT(*) FROM contacts WHERE public_key = :publicKey")
    fun exists(publicKey: ByteArray): Boolean

    @Query("SELECT * FROM contacts WHERE public_key = :publicKey")
    fun load(publicKey: ByteArray): Contact

    @Query("SELECT * FROM contacts WHERE friend_number = :friendNumber")
    fun load(friendNumber: Int): Contact

    @Query("SELECT * FROM contacts")
    fun loadAll(): List<Contact>
}
