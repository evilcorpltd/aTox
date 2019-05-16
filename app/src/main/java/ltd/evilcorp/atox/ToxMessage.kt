package ltd.evilcorp.atox

enum class MessageType {
    SendMessage
}

class ToxMessage(val type: MessageType, val param1: Int, val param2: Int, val data: Object?)
