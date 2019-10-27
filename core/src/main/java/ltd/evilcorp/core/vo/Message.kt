package ltd.evilcorp.core.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Sender {
    Sent,
    Received,
}

@Entity(tableName = "messages")
data class Message(
    @ColumnInfo(name = "conversation")
    val publicKey: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "sender")
    val sender: Sender,

    @ColumnInfo(name = "correlation_id")
    var correlationId: Int,

    @ColumnInfo(name = "timestamp")
    var timestamp: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}
