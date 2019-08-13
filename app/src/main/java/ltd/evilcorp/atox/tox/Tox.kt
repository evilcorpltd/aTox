package ltd.evilcorp.atox.tox

import android.util.Log
import im.tox.tox4j.core.enums.ToxFileControl
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

    fun getToxId() = ToxID.fromBytes(tox.address)
    fun getPublicKey() = PublicKey.fromBytes(tox.publicKey)

    fun addContact(toxId: ToxID, message: String) {
        tox.addFriend(toxId.bytes(), message.toByteArray())
        updateContactMapping()
    }

    fun deleteContact(publicKey: PublicKey) {
        Log.e(TAG, "Deleting $publicKey")
        tox.friendList.find { PublicKey.fromBytes(tox.getFriendPublicKey(it)) == publicKey }?.let { friend ->
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
            Log.i(TAG, "${friendNumbers[it]}: ${tox.getFriendPublicKey(friendNumbers[it]).bytesToHex()}")
            Pair(PublicKey.fromBytes(tox.getFriendPublicKey(friendNumbers[it])), friendNumbers[it])
        }
    }

    fun sendMessage(publicKey: PublicKey, message: String): Int = tox.friendSendMessage(
        contactByKey(publicKey),
        ToxMessageType.NORMAL,
        0,
        message.toByteArray()
    )

    fun save() = saveManager.save(getPublicKey(), tox.savedata)

    fun acceptFriendRequest(publicKey: PublicKey) {
        tox.addFriendNorequest(publicKey.bytes())
        updateContactMapping()
    }

    fun startFileTransfer(publicKey: PublicKey, fileNumber: Int) =
        tox.fileControl(contactByKey(publicKey), fileNumber, ToxFileControl.RESUME)

    fun stopFileTransfer(publicKey: PublicKey, fileNumber: Int) =
        tox.fileControl(contactByKey(publicKey), fileNumber, ToxFileControl.CANCEL)

    private fun contactByKey(publicKey: PublicKey): Int = tox.friendByPublicKey(publicKey.bytes())
}
