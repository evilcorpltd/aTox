package ltd.evilcorp.atox.vo

enum class Sender {
    Sent,
    Received,
}

class MessageModel(val message: String, val sender: Sender)
