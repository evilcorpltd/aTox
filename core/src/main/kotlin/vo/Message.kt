// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Sender {
    Sent,
    Received,
}

enum class MessageType {
    Normal,
    Action,
    FileTransfer,
}

@Entity(tableName = "messages")
data class Message(
    @ColumnInfo(name = "conversation")
    val publicKey: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "sender")
    val sender: Sender,

    @ColumnInfo(name = "type")
    val type: MessageType,

    @ColumnInfo(name = "correlation_id")
    var correlationId: Int,

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = 0,
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}
