// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.UserStatus

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(contact: Contact)

    @Update
    fun update(contact: Contact)

    @Delete
    fun delete(contact: Contact)

    @Query("SELECT COUNT(*) FROM contacts WHERE public_key = :publicKey")
    fun exists(publicKey: String): Boolean

    @Query("SELECT * FROM contacts WHERE public_key = :publicKey")
    fun load(publicKey: String): Flow<Contact>

    @Query("SELECT * FROM contacts")
    fun loadAll(): Flow<List<Contact>>

    @Query("UPDATE contacts SET connection_status = :status, typing = :typing")
    fun resetTransientData(
        status: ConnectionStatus = ConnectionStatus.None,
        typing: Boolean = false,
    )

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

    @Query("UPDATE contacts SET draft_message = :draft WHERE public_key = :publicKey")
    fun setDraftMessage(publicKey: String, draft: String)
}
