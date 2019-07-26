package ltd.evilcorp.atox.tox

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxUserStatus
import im.tox.tox4j.core.options.ProxyOptions
import im.tox.tox4j.core.options.SaveDataOptions
import im.tox.tox4j.core.options.ToxOptions
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.UserStatus

fun String.hexToByteArray(): ByteArray =
    chunked(2).map { it.toUpperCase().toInt(16).toByte() }.toByteArray()

fun ByteArray.byteArrayToHex(): String = this.joinToString("") { "%02X".format(it) }

fun ToxUserStatus.toUserStatus(): UserStatus {
    return UserStatus.values()[this.ordinal]
}

fun ToxConnection.toConnectionStatus(): ConnectionStatus {
    return ConnectionStatus.values()[this.ordinal]
}

fun SaveOptions.toToxOptions(): ToxOptions = ToxOptions(
    true,
    true,
    true,
    ProxyOptions.`None$`(),
    0,
    0,
    0,
    if (saveData == null) SaveDataOptions.`None$`() else SaveDataOptions.ToxSave(saveData),
    true
)
