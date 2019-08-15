package ltd.evilcorp.core.db

import androidx.lifecycle.LiveData
import androidx.room.*
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact

@Dao
internal interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(contact: Contact)

    @Update
    fun update(contact: Contact)

    @Delete
    fun delete(contact: Contact)

    @Query("SELECT COUNT(*) FROM contacts WHERE public_key = :publicKey")
    fun exists(publicKey: String): Boolean

    @Query("SELECT * FROM contacts WHERE public_key = :publicKey")
    fun load(publicKey: String): LiveData<Contact>

    @Query("SELECT * FROM contacts")
    fun loadAll(): LiveData<List<Contact>>

    @Query("UPDATE contacts SET connection_status = :status, typing = :typing")
    fun resetTransientData(status: ConnectionStatus = ConnectionStatus.None, typing: Boolean = false)

    @Query("UPDATE contacts SET avatar_uri = :uri WHERE public_key = :publicKey")
    fun setAvatarUri(publicKey: String, uri: String)
}
