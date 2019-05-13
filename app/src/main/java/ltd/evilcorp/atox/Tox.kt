package ltd.evilcorp.atox

import android.util.Log
import im.tox.tox4j.core.callbacks.ToxCoreEventListener
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxFileControl
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.core.enums.ToxUserStatus
import im.tox.tox4j.impl.jni.ToxCoreImpl
import im.tox.tox4j.core.options.ToxOptions
import java.io.File

private class NoToxEventListener : ToxCoreEventListener<Int> {
    override fun friendLosslessPacket(p0: Int, p1: ByteArray, p2: Int?): Int {
        return Log.e("ToxCore", "friendLosslessPacket")
    }

    override fun fileRecvControl(p0: Int, p1: Int, p2: ToxFileControl, p3: Int?): Int {
        return Log.e("ToxCore", "fileRecvControl")
    }

    override fun friendStatusMessage(p0: Int, p1: ByteArray, p2: Int?): Int {
        return Log.e("ToxCore", "friendStatusMessage")
    }

    override fun friendReadReceipt(p0: Int, p1: Int, p2: Int?): Int {
        return Log.e("ToxCore", "friendReadReceipt")
    }

    override fun friendStatus(p0: Int, p1: ToxUserStatus, p2: Int?): Int {
        return Log.e("ToxCore", "friendStatus")
    }

    override fun friendConnectionStatus(p0: Int, p1: ToxConnection, p2: Int?): Int {
        return Log.e("ToxCore", "friendConnectionStatus")
    }

    override fun friendRequest(p0: ByteArray, p1: Int, p2: ByteArray, p3: Int?): Int {
        return Log.e("ToxCore", "friendRequest")
    }

    override fun friendMessage(p0: Int, p1: ToxMessageType, p2: Int, p3: ByteArray, p4: Int?): Int {
        return Log.e("ToxCore", "friendMessage")
    }

    override fun friendName(p0: Int, p1: ByteArray, p2: Int?): Int {
        return Log.e("ToxCore", "friendName")
    }

    override fun fileRecvChunk(p0: Int, p1: Int, p2: Long, p3: ByteArray, p4: Int?): Int {
        return Log.e("ToxCore", "fileRecvChunk")
    }

    override fun fileRecv(p0: Int, p1: Int, p2: Int, p3: Long, p4: ByteArray, p5: Int?): Int {
        return Log.e("ToxCore", "fileRecv")
    }

    override fun friendLossyPacket(p0: Int, p1: ByteArray, p2: Int?): Int {
        return Log.e("ToxCore", "friendLossyPacket")
    }

    override fun selfConnectionStatus(p0: ToxConnection, p1: Int?): Int {
        return Log.e("ToxCore", "selfConnectionStatus")
    }

    override fun friendTyping(p0: Int, p1: Boolean, p2: Int?): Int {
        return Log.e("ToxCore", "friendTyping")
    }

    override fun fileChunkRequest(p0: Int, p1: Int, p2: Long, p3: Int, p4: Int?): Int {
        return Log.e("ToxCore", "fileChunkRequest")
    }
}

class Tox(options: ToxOptions) {
    private val tox: ToxCoreImpl = ToxCoreImpl(options)

    fun bootstrap(address: String, port: Int, publicKey: ByteArray) {
        tox.bootstrap(address, port, publicKey)
        tox.addTcpRelay(address, port, publicKey)
    }

    fun iterate(): Int {
        tox.iterate(NoToxEventListener(), null)
        return tox.iterationInterval()
    }

    fun setName(name: String) {
        tox.name = name.toByteArray()
    }

    fun getName(): String {
        return String(tox.name)
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
