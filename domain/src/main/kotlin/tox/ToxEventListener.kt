// SPDX-FileCopyrightText: 2019 aTox contributors
// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import im.tox.tox4j.core.callbacks.ToxCoreEventListener
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxFileControl
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.core.enums.ToxUserStatus
import javax.inject.Inject
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.core.vo.UserStatus

typealias FriendLosslessPacketHandler = (pk: PublicKey, data: ByteArray) -> Unit
typealias FileRecvControlHandler = (pk: PublicKey, fileNo: Int, control: ToxFileControl) -> Unit
typealias FriendStatusMessageHandler = (pk: PublicKey, message: String) -> Unit
typealias FriendReadReceiptHandler = (pk: PublicKey, messageId: Int) -> Unit
typealias FriendStatusHandler = (pk: PublicKey, status: UserStatus) -> Unit
typealias FriendConnectionStatusHandler = (pk: PublicKey, status: ConnectionStatus) -> Unit
typealias FriendRequestHandler = (pk: PublicKey, timeDelta: Int, message: String) -> Unit
typealias FriendMessageHandler = (
    pk: PublicKey,
    messageType: ToxMessageType,
    timeDelta: Int,
    message: String,
) -> Unit

typealias FriendNameHandler = (pk: PublicKey, newName: String) -> Unit
typealias FileRecvChunkHandler = (pk: PublicKey, fileNo: Int, position: Long, data: ByteArray) -> Unit
typealias FileRecvHandler = (pk: PublicKey, fileNo: Int, kind: Int, size: Long, name: String) -> Unit
typealias FriendLossyPacketHandler = (pk: PublicKey, data: ByteArray) -> Unit
typealias SelfConnectionStatusHandler = (status: ConnectionStatus) -> Unit
typealias FriendTypingHandler = (pk: PublicKey, isTyping: Boolean) -> Unit
typealias FileChunkRequestHandler = (pk: PublicKey, fileNo: Int, position: Long, length: Int) -> Unit

class ToxEventListener @Inject constructor() : ToxCoreEventListener<Unit> {
    var contactMapping: List<Pair<PublicKey, Int>> = listOf()

    var friendLosslessPacketHandler: FriendLosslessPacketHandler = { _, _ -> }
    var fileRecvControlHandler: FileRecvControlHandler = { _, _, _ -> }
    var friendStatusMessageHandler: FriendStatusMessageHandler = { _, _ -> }
    var friendReadReceiptHandler: FriendReadReceiptHandler = { _, _ -> }
    var friendStatusHandler: FriendStatusHandler = { _, _ -> }
    var friendConnectionStatusHandler: FriendConnectionStatusHandler = { _, _ -> }
    var friendRequestHandler: FriendRequestHandler = { _, _, _ -> }
    var friendMessageHandler: FriendMessageHandler = { _, _, _, _ -> }
    var friendNameHandler: FriendNameHandler = { _, _ -> }
    var fileRecvChunkHandler: FileRecvChunkHandler = { _, _, _, _ -> }
    var fileRecvHandler: FileRecvHandler = { _, _, _, _, _ -> }
    var friendLossyPacketHandler: FriendLossyPacketHandler = { _, _ -> }
    var selfConnectionStatusHandler: SelfConnectionStatusHandler = { _ -> }
    var friendTypingHandler: FriendTypingHandler = { _, _ -> }
    var fileChunkRequestHandler: FileChunkRequestHandler = { _, _, _, _ -> }

    private fun keyFor(friendNo: Int) = contactMapping.find { it.second == friendNo }!!.first

    override fun friendLosslessPacket(friendNo: Int, data: ByteArray, s: Unit?) =
        friendLosslessPacketHandler(keyFor(friendNo), data)

    override fun fileRecvControl(friendNo: Int, fileNo: Int, control: ToxFileControl, s: Unit?) =
        fileRecvControlHandler(keyFor(friendNo), fileNo, control)

    override fun friendStatusMessage(friendNo: Int, message: ByteArray, s: Unit?) =
        friendStatusMessageHandler(keyFor(friendNo), String(message))

    override fun friendReadReceipt(friendNo: Int, messageId: Int, s: Unit?): Unit =
        friendReadReceiptHandler(keyFor(friendNo), messageId)

    override fun friendStatus(friendNo: Int, status: ToxUserStatus, s: Unit?) =
        friendStatusHandler(keyFor(friendNo), status.toUserStatus())

    override fun friendConnectionStatus(friendNo: Int, status: ToxConnection, s: Unit?) =
        friendConnectionStatusHandler(keyFor(friendNo), status.toConnectionStatus())

    override fun friendRequest(publicKey: ByteArray, timeDelta: Int, message: ByteArray, s: Unit?) =
        friendRequestHandler(PublicKey.fromBytes(publicKey), timeDelta, String(message))

    override fun friendMessage(friendNo: Int, type: ToxMessageType, timeDelta: Int, message: ByteArray, s: Unit?) =
        friendMessageHandler(keyFor(friendNo), type, timeDelta, String(message))

    override fun friendName(friendNo: Int, newName: ByteArray, s: Unit?) =
        friendNameHandler(keyFor(friendNo), String(newName))

    override fun fileRecvChunk(friendNo: Int, fileNo: Int, position: Long, data: ByteArray, s: Unit?) =
        fileRecvChunkHandler(keyFor(friendNo), fileNo, position, data)

    override fun fileRecv(friendNo: Int, fileNo: Int, kind: Int, fileSize: Long, filename: ByteArray, s: Unit?) =
        fileRecvHandler(keyFor(friendNo), fileNo, kind, fileSize, String(filename))

    override fun friendLossyPacket(friendNo: Int, data: ByteArray, s: Unit?) =
        friendLossyPacketHandler(keyFor(friendNo), data)

    override fun selfConnectionStatus(connectionStatus: ToxConnection, s: Unit?) =
        selfConnectionStatusHandler(connectionStatus.toConnectionStatus())

    override fun friendTyping(friendNo: Int, isTyping: Boolean, s: Unit?) =
        friendTypingHandler(keyFor(friendNo), isTyping)

    override fun fileChunkRequest(friendNo: Int, fileNo: Int, position: Long, length: Int, s: Unit?) =
        fileChunkRequestHandler(keyFor(friendNo), fileNo, position, length)
}
