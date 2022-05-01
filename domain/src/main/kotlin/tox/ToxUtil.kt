// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxFileKind
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.core.enums.ToxUserStatus
import im.tox.tox4j.core.options.ProxyOptions
import im.tox.tox4j.core.options.SaveDataOptions
import im.tox.tox4j.core.options.ToxOptions
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.FileKind
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.toHex

fun String.hexToBytes(): ByteArray = chunked(2).map { it.uppercase().toHex().toByte() }.toByteArray()
fun ByteArray.bytesToHex(): String = this.joinToString("") { "%02X".format(it) }
fun ToxUserStatus.toUserStatus(): UserStatus = UserStatus.values()[this.ordinal]
fun ToxConnection.toConnectionStatus(): ConnectionStatus = ConnectionStatus.values()[this.ordinal]
fun ToxMessageType.toMessageType(): MessageType = MessageType.values()[this.ordinal]
fun SaveOptions.toToxOptions(): ToxOptions = ToxOptions(
    true,
    udpEnabled,
    true,
    when (proxyType) {
        ProxyType.None -> ProxyOptions.`None$`()
        ProxyType.HTTP -> ProxyOptions.Http(proxyAddress, proxyPort)
        ProxyType.SOCKS5 -> ProxyOptions.Socks5(proxyAddress, proxyPort)
    },
    0,
    0,
    0,
    if (saveData == null) SaveDataOptions.`None$`() else SaveDataOptions.ToxSave(saveData),
    true,
)

fun UserStatus.toToxType(): ToxUserStatus = when (this) {
    UserStatus.None -> ToxUserStatus.NONE
    UserStatus.Away -> ToxUserStatus.AWAY
    UserStatus.Busy -> ToxUserStatus.BUSY
}

fun MessageType.toToxType(): ToxMessageType = when (this) {
    MessageType.Normal -> ToxMessageType.NORMAL
    MessageType.Action -> ToxMessageType.ACTION
    MessageType.FileTransfer -> throw Exception("File transfer message type doesn't exist in Tox")
}

fun FileKind.toToxtype(): Int = when (this) {
    FileKind.Avatar -> ToxFileKind.AVATAR
    FileKind.Data -> ToxFileKind.DATA
}
