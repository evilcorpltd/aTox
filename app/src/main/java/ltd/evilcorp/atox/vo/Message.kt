package ltd.evilcorp.atox.vo

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
    val publicKey: ByteArray,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "sender")
    val sender: Sender
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (!publicKey.contentEquals(other.publicKey)) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.contentHashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}
