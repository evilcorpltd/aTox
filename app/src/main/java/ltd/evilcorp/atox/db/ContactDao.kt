package ltd.evilcorp.atox.db

import androidx.lifecycle.LiveData
import androidx.room.*
import ltd.evilcorp.atox.vo.Contact

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(contact: Contact)

    @Update
    fun update(contact: Contact)

    @Query("SELECT COUNT(*) FROM contacts WHERE public_key = :publicKey")
    fun exists(publicKey: ByteArray): Boolean

    @Query("SELECT * FROM contacts WHERE public_key = :publicKey")
    fun load(publicKey: ByteArray): LiveData<Contact>

    @Query("SELECT * FROM contacts WHERE friend_number = :friendNumber")
    fun load(friendNumber: Int): LiveData<Contact>

    @Query("SELECT * FROM contacts")
    fun loadAll(): LiveData<List<Contact>>
}
