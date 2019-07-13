package ltd.evilcorp.atox.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend_requests")
data class FriendRequest(
    @PrimaryKey
    @ColumnInfo(name = "public_key")
    val publicKey: String,
    @ColumnInfo(name = "message")
    val message: String = ""
)
