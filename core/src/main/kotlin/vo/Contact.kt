package ltd.evilcorp.core.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// These enums are 1:1 mappings of the enums in tox4j.
enum class ConnectionStatus {
    None,
    TCP,
    UDP,
}

enum class UserStatus {
    None,
    Away,
    Busy,
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
    var lastMessage: Long = 0,

    @ColumnInfo(name = "status")
    var status: UserStatus = UserStatus.None,

    @ColumnInfo(name = "connection_status")
    var connectionStatus: ConnectionStatus = ConnectionStatus.None,

    @ColumnInfo(name = "typing")
    var typing: Boolean = false,

    @ColumnInfo(name = "avatar_uri")
    var avatarUri: String = "",

    @ColumnInfo(name = "has_unread_messages")
    var hasUnreadMessages: Boolean = false,

    @ColumnInfo(name = "draft_message")
    var draftMessage: String = "",
)
