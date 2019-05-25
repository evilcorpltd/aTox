package ltd.evilcorp.atox

// TODO(robinlinden): This shouldn't depend on Tox types.
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxUserStatus

data class Contact(
    val publicKey: ByteArray,
    var friendNumber: Int = 0,
    var name: String = "Unknown",
    var status: ToxUserStatus = ToxUserStatus.NONE,
    var statusMessage: String = "...",
    var connectionStatus: ToxConnection = ToxConnection.NONE,
    var lastMessage: String = "Never",
    var typing: Boolean = false
) {
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
