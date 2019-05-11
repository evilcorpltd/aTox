package ltd.evilcorp.atox

enum class Sender {
    Sent,
    Received,
}

class MessageModel(val message: String, val sender: Sender)
