package ltd.evilcorp.domain.tox

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.core.enums.ToxUserStatus
import im.tox.tox4j.core.options.ProxyOptions
import im.tox.tox4j.core.options.SaveDataOptions
import im.tox.tox4j.core.options.ToxOptions
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.UserStatus
import java.util.*

fun String.hexToBytes(): ByteArray = chunked(2).map { it.toUpperCase(Locale.ROOT).toInt(16).toByte() }.toByteArray()
fun ByteArray.bytesToHex(): String = this.joinToString("") { "%02X".format(it) }
fun ToxUserStatus.toUserStatus(): UserStatus = UserStatus.values()[this.ordinal]
fun ToxConnection.toConnectionStatus(): ConnectionStatus = ConnectionStatus.values()[this.ordinal]
fun ToxMessageType.toMessageType(): MessageType = MessageType.values()[this.ordinal]
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

fun MessageType.toToxType(): ToxMessageType = when(this) {
    MessageType.Normal -> ToxMessageType.NORMAL
    MessageType.Action -> ToxMessageType.ACTION
}
