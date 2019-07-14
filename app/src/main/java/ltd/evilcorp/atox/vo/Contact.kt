package ltd.evilcorp.atox.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// These enums are 1:1 mappings of the enums in tox4j.
enum class ConnectionStatus {
    NONE,
    TCP,
    UDP,
}

enum class UserStatus {
    NONE,
    AWAY,
    BUSY,
}

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey
    @ColumnInfo(name = "public_key")
    val publicKey: ByteArray,
    @ColumnInfo(name = "name")
    var name: String = "Unknown",
    @ColumnInfo(name = "status_message")
    var statusMessage: String = "...",
    @ColumnInfo(name = "last_message")
    var lastMessage: String = "Never",
    @ColumnInfo(name = "status")
    var status: UserStatus = UserStatus.NONE,
    @ColumnInfo(name = "connection_status")
    var connectionStatus: ConnectionStatus = ConnectionStatus.NONE,
    @ColumnInfo(name = "typing")
    var typing: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contact

        if (!publicKey.contentEquals(other.publicKey)) return false
        if (name != other.name) return false
        if (statusMessage != other.statusMessage) return false
        if (lastMessage != other.lastMessage) return false
        if (status != other.status) return false
        if (connectionStatus != other.connectionStatus) return false
        if (typing != other.typing) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.contentHashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + statusMessage.hashCode()
        result = 31 * result + lastMessage.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + connectionStatus.hashCode()
        result = 31 * result + typing.hashCode()
        return result
    }
}
