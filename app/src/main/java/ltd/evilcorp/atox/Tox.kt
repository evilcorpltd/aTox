package ltd.evilcorp.atox

import android.util.Log
import im.tox.tox4j.core.callbacks.ToxCoreEventListener
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxFileControl
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.core.enums.ToxUserStatus
import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.impl.jni.ToxCoreImpl
import java.io.File

private class NoToxEventListener : ToxCoreEventListener<Int> {
    override fun friendLosslessPacket(friendNumber: Int, data: ByteArray, state: Int?): Int {
        return Log.e("ToxCore", "friendLosslessPacket")
    }

    override fun fileRecvControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl, state: Int?): Int {
        return Log.e("ToxCore", "fileRecvControl")
    }

    override fun friendStatusMessage(friendNumber: Int, message: ByteArray, state: Int?): Int {
        return Log.e("ToxCore", "friendStatusMessage")
    }

    override fun friendReadReceipt(friendNumber: Int, messageId: Int, state: Int?): Int {
        return Log.e("ToxCore", "friendReadReceipt")
    }

    override fun friendStatus(friendNumber: Int, status: ToxUserStatus, state: Int?): Int {
        return Log.e("ToxCore", "friendStatus")
    }

    override fun friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection, state: Int?): Int {
        return Log.e("ToxCore", "friendConnectionStatus")
    }

    override fun friendRequest(publicKey: ByteArray, timeDelta: Int, message: ByteArray, state: Int?): Int {
        return Log.e("ToxCore", "friendRequest")
    }

    override fun friendMessage(
        friendNumber: Int,
        messageType: ToxMessageType,
        timeDelta: Int,
        message: ByteArray,
        state: Int?
    ): Int {
        return Log.e("ToxCore", "friendMessage")
    }

    override fun friendName(friendNumber: Int, name: ByteArray, state: Int?): Int {
        return Log.e("ToxCore", "friendName")
    }

    override fun fileRecvChunk(friendNumber: Int, fileNumber: Int, position: Long, data: ByteArray, state: Int?): Int {
        return Log.e("ToxCore", "fileRecvChunk")
    }

    override fun fileRecv(
        friendNumber: Int,
        fileNumber: Int,
        kind: Int,
        fileSize: Long,
        filename: ByteArray,
        state: Int?
    ): Int {
        return Log.e("ToxCore", "fileRecv")
    }

    override fun friendLossyPacket(friendNumber: Int, data: ByteArray, state: Int?): Int {
        return Log.e("ToxCore", "friendLossyPacket")
    }

    override fun selfConnectionStatus(connectionStatus: ToxConnection, state: Int?): Int {
        return Log.e("ToxCore", "selfConnectionStatus")
    }

    override fun friendTyping(friendNumber: Int, isTyping: Boolean, state: Int?): Int {
        return Log.e("ToxCore", "friendTyping")
    }

    override fun fileChunkRequest(friendNumber: Int, fileNumber: Int, position: Long, length: Int, state: Int?): Int {
        return Log.e("ToxCore", "fileChunkRequest")
    }
}

class Tox(options: ToxOptions) {
    private val tox: ToxCoreImpl = ToxCoreImpl(options)

    fun bootstrap(address: String, port: Int, publicKey: ByteArray) {
        tox.bootstrap(address, port, publicKey)
        tox.addTcpRelay(address, port, publicKey)
    }

    fun iterate(): Int = tox.iterate(NoToxEventListener(), 42)
    fun iterationInterval(): Int = tox.iterationInterval()

    fun kill() {
        tox.close()
    }

    fun setName(name: String) {
        tox.name = name.toByteArray()
    }

    fun getName(): String {
        return String(tox.name)
    }

    fun addContact(toxId: String, message: String): Int {
        return tox.addFriend(toxId.hexToByteArray(), message.toByteArray())
    }

    fun sendMessage(friendNumber: Int, message: String): Int {
        return tox.friendSendMessage(friendNumber, ToxMessageType.NORMAL, 0, message.toByteArray())
    }

    fun save(destination: String, encrypt: Boolean) {
        val fileName = this.getName() + ".tox"

        val saveFile = File("$destination/$fileName")
        if (!saveFile.exists()) {
            saveFile.createNewFile()
        }

        Log.e("ToxCore", "Saving profile to $saveFile")

        saveFile.writeBytes(tox.savedata)
    }
}
