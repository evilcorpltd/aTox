package ltd.evilcorp.atox.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

// This is a 1:1 mapping of the ToxConnection enum in tox4j and needs to stay that way.
enum class ConnectionStatus {
    NONE,
    TCP,
    UDP,
}

// This is a 1:1 mapping of the ToxUserStatus enum in tox4j and needs to stay that way.
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
    @ColumnInfo(name = "friend_number")
    var friendNumber: Int = -1,
    @ColumnInfo(name = "name")
    var name: String = "Unknown",
    @ColumnInfo(name = "status_message")
    var statusMessage: String = "...",
    @ColumnInfo(name = "last_message")
    var lastMessage: String = "Never"
) {
    @Ignore
    var status: UserStatus = UserStatus.NONE
    @Ignore
    var connectionStatus: ConnectionStatus = ConnectionStatus.NONE
    @Ignore
    var typing: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contact

        if (!publicKey.contentEquals(other.publicKey)) return false
        if (friendNumber != other.friendNumber) return false
        if (name != other.name) return false
        if (status != other.status) return false
        if (statusMessage != other.statusMessage) return false
        if (connectionStatus != other.connectionStatus) return false
        if (lastMessage != other.lastMessage) return false
        if (typing != other.typing) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.contentHashCode()
        result = 31 * result + friendNumber
        result = 31 * result + name.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + statusMessage.hashCode()
        result = 31 * result + connectionStatus.hashCode()
        result = 31 * result + lastMessage.hashCode()
        result = 31 * result + typing.hashCode()
        return result
    }
}
