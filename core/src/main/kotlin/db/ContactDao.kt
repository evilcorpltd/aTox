// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
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
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.core.vo.UserStatus

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(contact: Contact)

    @Update
    fun update(contact: Contact)

    @Delete
    fun delete(contact: Contact)

    @Query("SELECT COUNT(*) FROM contacts WHERE public_key = :pk")
    fun exists(pk: PublicKey): Boolean

    @Query("SELECT * FROM contacts WHERE public_key = :pk")
    fun load(pk: PublicKey): Flow<Contact>

    @Query("SELECT * FROM contacts")
    fun loadAll(): Flow<List<Contact>>

    @Query("UPDATE contacts SET connection_status = :status, typing = :typing")
    fun resetTransientData(status: ConnectionStatus = ConnectionStatus.None, typing: Boolean = false)

    @Query("UPDATE contacts SET name = :name WHERE public_key = :pk")
    fun setName(pk: PublicKey, name: String)

    @Query("UPDATE contacts SET status_message = :statusMessage WHERE public_key = :pk")
    fun setStatusMessage(pk: PublicKey, statusMessage: String)

    @Query("UPDATE contacts SET last_message = :lastMessage WHERE public_key = :pk")
    fun setLastMessage(pk: PublicKey, lastMessage: Long)

    @Query("UPDATE contacts SET status = :status WHERE public_key = :pk")
    fun setUserStatus(pk: PublicKey, status: UserStatus)

    @Query("UPDATE contacts SET connection_status = :connectionStatus WHERE public_key = :pk")
    fun setConnectionStatus(pk: PublicKey, connectionStatus: ConnectionStatus)

    @Query("UPDATE contacts SET typing = :typing WHERE public_key = :pk")
    fun setTyping(pk: PublicKey, typing: Boolean)

    @Query("UPDATE contacts SET avatar_uri = :uri WHERE public_key = :pk")
    fun setAvatarUri(pk: PublicKey, uri: String)

    @Query("UPDATE contacts SET has_unread_messages = :anyUnread WHERE public_key = :pk")
    fun setHasUnreadMessages(pk: PublicKey, anyUnread: Boolean)

    @Query("UPDATE contacts SET draft_message = :draft WHERE public_key = :pk")
    fun setDraftMessage(pk: PublicKey, draft: String)
}
