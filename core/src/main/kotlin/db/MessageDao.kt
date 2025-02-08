// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.PublicKey

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(message: Message)

    @Query("SELECT * FROM messages WHERE conversation == :conversation")
    fun load(conversation: PublicKey): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE conversation == :conversation AND timestamp == 0")
    fun loadPending(conversation: PublicKey): List<Message>

    @Query("UPDATE messages SET correlation_id = :correlationId WHERE id == :id")
    fun setCorrelationId(id: Long, correlationId: Int)

    @Query("DELETE FROM messages WHERE conversation == :conversation")
    fun delete(conversation: PublicKey)

    @Suppress("ktlint:standard:max-line-length")
    @Query(
        "UPDATE messages SET timestamp = :timestamp WHERE conversation == :conversation AND correlation_id == :correlationId AND timestamp == 0",
    )
    fun setReceipt(conversation: PublicKey, correlationId: Int, timestamp: Long)

    @Query("DELETE FROM messages WHERE id = :id")
    fun deleteMessage(id: Long)
}
