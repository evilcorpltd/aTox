// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun save(user: User)

    @Update
    fun update(user: User)

    @Query("UPDATE users SET name = :name WHERE public_key == :pk")
    fun updateName(pk: PublicKey, name: String)

    @Query("UPDATE users SET status_message = :statusMessage WHERE public_key == :pk")
    fun updateStatusMessage(pk: PublicKey, statusMessage: String)

    @Query("UPDATE users SET connection_status = :connectionStatus WHERE public_key == :pk")
    fun updateConnection(pk: PublicKey, connectionStatus: ConnectionStatus)

    @Query("UPDATE users SET status = :status WHERE public_key == :pk")
    fun updateStatus(pk: PublicKey, status: UserStatus)

    @Query("SELECT COUNT(*) FROM users WHERE public_key = :pk")
    fun exists(pk: PublicKey): Boolean

    @Query("SELECT * FROM users WHERE public_key = :pk")
    fun load(pk: PublicKey): Flow<User>
}
