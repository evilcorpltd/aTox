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
    val publicKey: String,
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
)
