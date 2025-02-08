// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FileKind {
    Data,
    Avatar,
}

// Since the progress can't be negative, I'm reusing that part for some markers.
const val FT_STARTED = 0L
const val FT_NOT_STARTED = -1L
const val FT_REJECTED = -2L

@Entity(tableName = "file_transfers")
data class FileTransfer(
    @ColumnInfo(name = "public_key")
    val publicKey: PublicKey,

    @ColumnInfo(name = "file_number")
    val fileNumber: Int,

    @ColumnInfo(name = "file_kind")
    val fileKind: Int,

    @ColumnInfo(name = "file_size")
    val fileSize: Long,

    @ColumnInfo(name = "file_name")
    val fileName: String,

    @ColumnInfo(name = "outgoing")
    val outgoing: Boolean,

    @ColumnInfo(name = "progress")
    var progress: Long = FT_NOT_STARTED,

    @ColumnInfo(name = "destination")
    var destination: String = "",
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0
}

fun FileTransfer.isComplete() = progress >= fileSize
fun FileTransfer.isStarted() = progress >= FT_STARTED
fun FileTransfer.isRejected() = progress == FT_REJECTED
