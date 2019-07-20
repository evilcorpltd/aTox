package ltd.evilcorp.atox.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "public_key")
    val publicKey: String,

    @ColumnInfo(name = "name")
    var name: String = "aTox user",

    @ColumnInfo(name = "status_message")
    var statusMessage: String = "Brought to you live, by aTox",

    @ColumnInfo(name = "status")
    var status: UserStatus = UserStatus.NONE,

    @ColumnInfo(name = "connection_status")
    var connectionStatus: ConnectionStatus = ConnectionStatus.NONE,

    @ColumnInfo(name = "password")
    var password: String = ""
)
