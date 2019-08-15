package ltd.evilcorp.atox.tox

import android.util.Log
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.impl.jni.ToxCoreImpl

private const val TAG = "Tox"

class Tox(
    private val eventListener: ToxEventListener,
    private val saveManager: SaveManager,
    options: SaveOptions
) {
    private val tox: ToxCoreImpl = ToxCoreImpl(options.toToxOptions())

    init {
        eventListener.publicKey = getPublicKey()
        updateContactMapping()
    }

    private fun updateContactMapping() {
        eventListener.contactMapping = getContacts()
    }

    fun bootstrap(address: String, port: Int, publicKey: ByteArray) {
        tox.bootstrap(address, port, publicKey)
        tox.addTcpRelay(address, port, publicKey)
    }

    fun iterate(): Unit = tox.iterate(eventListener, Unit)
    fun iterationInterval(): Long = tox.iterationInterval().toLong()

    fun getName(): String = String(tox.name)
    fun setName(name: String) {
        tox.name = name.toByteArray()
    }

    fun getStatusMessage(): String = String(tox.statusMessage)

    fun getToxId(): String = tox.address.bytesToHex()
    fun getPublicKey(): String = tox.publicKey.bytesToHex()

    fun addContact(toxId: String, message: String) {
        tox.addFriend(toxId.hexToBytes(), message.toByteArray())
        updateContactMapping()
    }

    fun deleteContact(publicKey: String) {
        Log.e(TAG, "Deleting $publicKey")
        tox.friendList.find { tox.getFriendPublicKey(it).bytesToHex() == publicKey }?.let { friend ->
            tox.deleteFriend(friend)
        } ?: Log.e(
            TAG, "Tried to delete nonexistent contact, this can happen if the database is out of sync with the Tox save"
        )

        updateContactMapping()
    }

    fun getContacts(): List<Pair<String, Int>> {
        val friendNumbers = tox.friendList
        Log.i(TAG, "Loading ${friendNumbers.size} friends")
        return List(friendNumbers.size) {
            Log.i(TAG, "${friendNumbers[it]}: ${tox.getFriendPublicKey(friendNumbers[it]).bytesToHex()}")
            Pair(tox.getFriendPublicKey(friendNumbers[it]).bytesToHex(), friendNumbers[it])
        }
    }

    fun sendMessage(publicKey: String, message: String): Int = tox.friendSendMessage(
        tox.friendByPublicKey(publicKey.hexToBytes()),
        ToxMessageType.NORMAL,
        0,
        message.toByteArray()
    )

    fun save() = saveManager.save(getPublicKey(), tox.savedata)

    fun acceptFriendRequest(publicKey: String) {
        tox.addFriendNorequest(publicKey.hexToBytes())
        updateContactMapping()
    }
}
