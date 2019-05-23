package ltd.evilcorp.atox

// TODO(robinlinden): This shouldn't depend on Tox types.
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxUserStatus

class Contact(
    val publicKey: ByteArray,
    var friendNumber: Int = 0,
    var name: String = "Unknown",
    var status: ToxUserStatus = ToxUserStatus.NONE,
    var statusMessage: String = "...",
    var connectionStatus: ToxConnection = ToxConnection.NONE,
    var lastMessage: String = "Never",
    var typing: Boolean = false
)
