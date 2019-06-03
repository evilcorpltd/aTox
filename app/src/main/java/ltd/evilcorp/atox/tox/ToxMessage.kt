package ltd.evilcorp.atox.tox

data class MsgAddContact(val toxId: String, val message: String)
data class MsgSendMessage(val publicKey: String, val message: String)
