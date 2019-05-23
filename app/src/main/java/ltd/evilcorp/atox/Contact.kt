package ltd.evilcorp.atox

class Contact(
    val publicKey: ByteArray,
    var name: String = "Unknown",
    var lastMessage: String = "Never",
    var friendNumber: Int = 0
)
