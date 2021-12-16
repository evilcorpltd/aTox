// SPDX-FileCopyrightText: 2019-2021 aTox contributors
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
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun save(user: User)

    @Update
    fun update(user: User)

    @Query("UPDATE users SET name = :name WHERE public_key == :publicKey")
    fun updateName(publicKey: String, name: String)

    @Query("UPDATE users SET status_message = :statusMessage WHERE public_key == :publicKey")
    fun updateStatusMessage(publicKey: String, statusMessage: String)

    @Query("UPDATE users SET connection_status = :connectionStatus WHERE public_key == :publicKey")
    fun updateConnection(publicKey: String, connectionStatus: ConnectionStatus)

    @Query("UPDATE users SET status = :status WHERE public_key == :publicKey")
    fun updateStatus(publicKey: String, status: UserStatus)

    @Query("UPDATE contacts SET avatar_uri = :uri WHERE public_key = :publicKey")
    fun updateAvatarUri(publicKey: String, uri: String)

    @Query("SELECT COUNT(*) FROM users WHERE public_key = :publicKey")
    fun exists(publicKey: String): Boolean

    @Query("SELECT * FROM users WHERE public_key = :publicKey")
    fun load(publicKey: String): Flow<User>
}
