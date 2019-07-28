package ltd.evilcorp.core.vo

import androidx.room.ColumnInfo
import androidx.room.Entity

enum class FileKind {
    Data,
    Avatar,
}

@Entity(tableName = "file_transfers", primaryKeys = ["public_key", "file_number"])
data class FileTransfer(
    @ColumnInfo(name = "public_key")
    val publicKey: String,

    @ColumnInfo(name = "file_number")
    val fileNumber: Int,

    @ColumnInfo(name = "file_kind")
    val fileKind: Int,

    @ColumnInfo(name = "file_size")
    val fileSize: Long,

    @ColumnInfo(name = "file_name")
    val fileName: String,

    @ColumnInfo(name = "outgoing")
    val outgoing: Boolean
)
