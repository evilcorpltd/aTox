// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import android.util.Log
import im.tox.tox4j.av.enums.ToxavCallControl
import im.tox.tox4j.core.enums.ToxFileControl
import im.tox.tox4j.core.exceptions.ToxFileControlException
import im.tox.tox4j.core.exceptions.ToxFileSendChunkException
import im.tox.tox4j.core.exceptions.ToxFriendAddException
import im.tox.tox4j.core.exceptions.ToxFriendCustomPacketException
import im.tox.tox4j.impl.jni.ToxAvImpl
import im.tox.tox4j.impl.jni.ToxCoreImpl
import kotlin.random.Random
import ltd.evilcorp.core.vo.FileKind
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.UserStatus

private const val TAG = "ToxWrapper"

private const val AUDIO_BIT_RATE = 128

enum class CustomPacketError {
    Success,
    Empty,
    FriendNotConnected,
    FriendNotFound,
    Invalid,
    Null,
    Sendq,
    TooLong,
}

class ToxWrapper(options: SaveOptions) {
    private val tox: ToxCoreImpl =
        ToxCoreImpl(
            options.toToxOptions()
                .also { Log.i(TAG, "Starting Tox with options $it") }
        )
    private val av: ToxAvImpl = ToxAvImpl(tox)

    init {
        updateContactMapping()
    }

    private fun updateContactMapping() {
        val contacts = getContacts()
        ToxEventListener.contactMapping = contacts
        ToxAvEventListener.contactMapping = contacts
    }

    fun bootstrap(address: String, port: Int, publicKey: ByteArray) {
        tox.bootstrap(address, port, publicKey)
        tox.addTcpRelay(address, port, publicKey)
    }

    fun stop() {
        av.close()
        tox.close()
    }

    fun iterate(): Unit = tox.iterate(ToxEventListener, Unit)
    fun iterateAv(): Unit = av.iterate(ToxAvEventListener, Unit)
    fun iterationInterval(): Long = tox.iterationInterval().toLong()
    fun iterationIntervalAv(): Long = av.iterationInterval().toLong()

    fun getName(): String = String(tox.name)
    fun setName(name: String) {
        tox.name = name.toByteArray()
    }

    fun getStatusMessage(): String = String(tox.statusMessage)
    fun setStatusMessage(statusMessage: String) {
        tox.statusMessage = statusMessage.toByteArray()
    }

    fun getToxId() = ToxID.fromBytes(tox.address)
    fun getPublicKey() = PublicKey.fromBytes(tox.publicKey)
    fun getNospam(): Int = tox.nospam
    fun setNospam(value: Int) {
        tox.nospam = value
    }

    fun getSaveData() = tox.savedata

    fun addContact(toxId: ToxID, message: String) {
        tox.addFriend(toxId.bytes(), message.toByteArray())
        updateContactMapping()
    }

    fun deleteContact(pk: PublicKey) {
        Log.i(TAG, "Deleting ${pk.fingerprint()}")
        tox.friendList.find { PublicKey.fromBytes(tox.getFriendPublicKey(it)) == pk }?.let { friend ->
            tox.deleteFriend(friend)
        } ?: Log.e(
            TAG, "Tried to delete nonexistent contact, this can happen if the database is out of sync with the Tox save"
        )

        updateContactMapping()
    }

    fun getContacts(): List<Pair<PublicKey, Int>> {
        val friendNumbers = tox.friendList
        Log.i(TAG, "Loading ${friendNumbers.size} friends")
        return List(friendNumbers.size) {
            Pair(PublicKey.fromBytes(tox.getFriendPublicKey(friendNumbers[it])), friendNumbers[it])
        }
    }

    fun sendMessage(publicKey: PublicKey, message: String, type: MessageType): Int = tox.friendSendMessage(
        contactByKey(publicKey),
        type.toToxType(),
        0,
        message.toByteArray()
    )

    fun acceptFriendRequest(pk: PublicKey) = try {
        tox.addFriendNorequest(pk.bytes())
        updateContactMapping()
    } catch (e: ToxFriendAddException) {
        Log.e(TAG, "Exception while accepting friend request $pk: $e")
    }

    fun startFileTransfer(pk: PublicKey, fileNumber: Int) = try {
        tox.fileControl(contactByKey(pk), fileNumber, ToxFileControl.RESUME)
    } catch (e: ToxFileControlException) {
        Log.e(TAG, "Error starting ft ${pk.fingerprint()} $fileNumber\n$e")
    }

    fun stopFileTransfer(pk: PublicKey, fileNumber: Int) = try {
        tox.fileControl(contactByKey(pk), fileNumber, ToxFileControl.CANCEL)
    } catch (e: ToxFileControlException) {
        Log.e(TAG, "Error stopping ft ${pk.fingerprint()} $fileNumber\n$e")
    }

    fun sendFile(pk: PublicKey, fileKind: FileKind, fileSize: Long, fileName: String) = try {
        tox.fileSend(contactByKey(pk), fileKind.toToxtype(), fileSize, Random.nextBytes(32), fileName.toByteArray())
    } catch (e: ToxFileControlException) {
        Log.e(TAG, "Error sending ft $fileName ${pk.fingerprint()}\n$e")
    }

    fun sendFileChunk(pk: PublicKey, fileNo: Int, pos: Long, data: ByteArray) = try {
        tox.fileSendChunk(contactByKey(pk), fileNo, pos, data)
    } catch (e: ToxFileSendChunkException) {
        Log.e(TAG, "Error sending chunk $pos:${data.size} to ${pk.fingerprint()} $fileNo\n$e")
    }

    fun setTyping(publicKey: PublicKey, typing: Boolean) = tox.setTyping(contactByKey(publicKey), typing)

    fun getStatus() = tox.status.toUserStatus()
    fun setStatus(status: UserStatus) {
        tox.status = status.toToxType()
    }

    fun sendLosslessPacket(pk: PublicKey, packet: ByteArray): CustomPacketError = try {
        tox.friendSendLosslessPacket(contactByKey(pk), packet)
        CustomPacketError.Success
    } catch (e: ToxFriendCustomPacketException) {
        when (e.code()) {
            ToxFriendCustomPacketException.Code.EMPTY -> CustomPacketError.Empty
            ToxFriendCustomPacketException.Code.FRIEND_NOT_CONNECTED -> CustomPacketError.FriendNotConnected
            ToxFriendCustomPacketException.Code.FRIEND_NOT_FOUND -> CustomPacketError.FriendNotFound
            ToxFriendCustomPacketException.Code.INVALID -> CustomPacketError.Invalid
            ToxFriendCustomPacketException.Code.NULL -> CustomPacketError.Null
            ToxFriendCustomPacketException.Code.SENDQ -> CustomPacketError.Sendq
            ToxFriendCustomPacketException.Code.TOO_LONG -> CustomPacketError.TooLong
            null -> TODO()
        }
    }

    private fun contactByKey(pk: PublicKey): Int = tox.friendByPublicKey(pk.bytes())

    // ToxAv, probably move these.
    fun startCall(pk: PublicKey) = av.call(contactByKey(pk), AUDIO_BIT_RATE, 0)
    fun answerCall(pk: PublicKey) = av.answer(contactByKey(pk), AUDIO_BIT_RATE, 0)
    fun endCall(pk: PublicKey) = av.callControl(contactByKey(pk), ToxavCallControl.CANCEL)
    fun sendAudio(pk: PublicKey, pcm: ShortArray, channels: Int, samplingRate: Int) =
        av.audioSendFrame(contactByKey(pk), pcm, pcm.size, channels, samplingRate)
}
