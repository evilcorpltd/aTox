package ltd.evilcorp.atox.vo

enum class Sender {
    Sent,
    Received,
}

class Message(val message: String, val sender: Sender)
