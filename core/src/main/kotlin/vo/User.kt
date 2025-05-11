// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "public_key")
    val publicKey: PublicKey,

    @ColumnInfo(name = "name")
    var name: String = "aTox user",

    @ColumnInfo(name = "status_message")
    var statusMessage: String = "Brought to you live, by aTox",

    @ColumnInfo(name = "status")
    var status: UserStatus = UserStatus.None,

    @ColumnInfo(name = "connection_status")
    var connectionStatus: ConnectionStatus = ConnectionStatus.None,

    @ColumnInfo(name = "password")
    var password: String = "",
)
