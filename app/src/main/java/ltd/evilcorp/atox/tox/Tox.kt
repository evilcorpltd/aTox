package ltd.evilcorp.atox.tox

import android.content.Context
import android.util.Log
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.impl.jni.ToxCoreImpl

class Tox(
    private val context: Context,
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

    fun iterate(): Int = tox.iterate(eventListener, 42)
    fun iterationInterval(): Int = tox.iterationInterval()

    fun setName(name: String) {
        tox.name = name.toByteArray()
    }

    fun getName(): String {
        return String(tox.name)
    }

    fun getStatusMessage(): String = String(tox.statusMessage)

    fun getToxId(): String = tox.address.byteArrayToHex()
    fun getPublicKey(): String = tox.publicKey.byteArrayToHex()

    fun addContact(toxId: String, message: String) {
        tox.addFriend(toxId.hexToByteArray(), message.toByteArray())
        updateContactMapping()
    }

    fun deleteContact(publicKey: String) {
        Log.e("Tox", "Deleting $publicKey")
        tox.friendList.find { tox.getFriendPublicKey(it).byteArrayToHex() == publicKey }?.let { friend ->
            tox.deleteFriend(friend)
        } ?: Log.e(
            "Tox",
            "Tried to delete nonexistent contact, this can happen if the database is out of sync with the Tox save"
        )

        updateContactMapping()
    }

    fun getContacts(): List<Pair<String, Int>> {
        val friendNumbers = tox.friendList
        Log.e("Tox", "Loading ${friendNumbers.size} friends")
        return List(friendNumbers.size) {
            Log.e("Tox", "${friendNumbers[it]}: ${tox.getFriendPublicKey(friendNumbers[it]).byteArrayToHex()}")
            Pair(tox.getFriendPublicKey(friendNumbers[it]).byteArrayToHex(), friendNumbers[it])
        }
    }

    fun sendMessage(publicKey: String, message: String): Int {
        return tox.friendSendMessage(
            tox.friendByPublicKey(publicKey.hexToByteArray()),
            ToxMessageType.NORMAL,
            0,
            message.toByteArray()
        )
    }

    fun save() = saveManager.save(getPublicKey(), tox.savedata)

    fun acceptFriendRequest(publicKey: String) {
        tox.addFriendNorequest(publicKey.hexToByteArray())
        updateContactMapping()
    }
}
