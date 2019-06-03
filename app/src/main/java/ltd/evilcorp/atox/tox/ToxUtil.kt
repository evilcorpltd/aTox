package ltd.evilcorp.atox.tox

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxUserStatus
import ltd.evilcorp.atox.vo.ConnectionStatus
import ltd.evilcorp.atox.vo.UserStatus


fun String.hexToByteArray(): ByteArray =
    chunked(2).map { it.toUpperCase().toInt(16).toByte() }.toByteArray()

fun ByteArray.byteArrayToHex(): String = this.joinToString("") { "%02x".format(it) }

fun ToxUserStatus.toUserStatus(): UserStatus {
    return UserStatus.values()[this.ordinal]
}

fun ToxConnection.toConnectionStatus(): ConnectionStatus {
    return ConnectionStatus.values()[this.ordinal]
}
