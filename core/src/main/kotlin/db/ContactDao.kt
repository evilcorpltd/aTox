package ltd.evilcorp.core.db

import androidx.lifecycle.LiveData
import androidx.room.*
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.UserStatus

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


    @Query("UPDATE contacts SET name = :name WHERE public_key = :publicKey")
    fun setName(publicKey: String, name: String)

    @Query("UPDATE contacts SET status_message = :statusMessage WHERE public_key = :publicKey")
    fun setStatusMessage(publicKey: String, statusMessage: String)

    @Query("UPDATE contacts SET last_message = :lastMessage WHERE public_key = :publicKey")
    fun setLastMessage(publicKey: String, lastMessage: Long)

    @Query("UPDATE contacts SET status = :status WHERE public_key = :publicKey")
    fun setUserStatus(publicKey: String, status: UserStatus)

    @Query("UPDATE contacts SET connection_status = :connectionStatus WHERE public_key = :publicKey")
    fun setConnectionStatus(publicKey: String, connectionStatus: ConnectionStatus)

    @Query("UPDATE contacts SET typing = :typing WHERE public_key = :publicKey")
    fun setTyping(publicKey: String, typing: Boolean)

    @Query("UPDATE contacts SET avatar_uri = :uri WHERE public_key = :publicKey")
    fun setAvatarUri(publicKey: String, uri: String)

    @Query("UPDATE contacts SET has_unread_messages = :anyUnread WHERE public_key = :publicKey")
    fun setHasUnreadMessages(publicKey: String, anyUnread: Boolean)
}
