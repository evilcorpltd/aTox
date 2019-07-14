package ltd.evilcorp.atox.tox

import android.util.Log
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.impl.jni.ToxCoreImpl
import ltd.evilcorp.atox.repository.ContactRepository
import ltd.evilcorp.atox.repository.FriendRequestRepository
import ltd.evilcorp.atox.repository.MessageRepository
import ltd.evilcorp.atox.ui.NotificationHelper
import java.io.File

class Tox(
    options: ToxOptions,
    contactRepository: ContactRepository,
    friendRequestRepository: FriendRequestRepository,
    messageRepository: MessageRepository,
    notificationHelper: NotificationHelper
) {
    private val tox: ToxCoreImpl = ToxCoreImpl(options)
    private val eventListener = ToxEventListener(
        contactRepository, friendRequestRepository, messageRepository, notificationHelper
    )

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

    fun kill() {
        tox.close()
    }

    fun setName(name: String) {
        tox.name = name.toByteArray()
    }

    fun getName(): String {
        return String(tox.name)
    }

    fun getToxId(): String = tox.address.byteArrayToHex()
    private fun getPublicKey(): String = tox.publicKey.byteArrayToHex()

    fun addContact(toxId: String, message: String) {
        tox.addFriend(toxId.hexToByteArray(), message.toByteArray())
        updateContactMapping()
    }

    fun deleteContact(publicKey: String) {
        Log.e("Tox", "Deleting $publicKey")
        val friend = tox.friendList.find { tox.getFriendPublicKey(it).byteArrayToHex() == publicKey }!!
        tox.deleteFriend(friend)
        updateContactMapping()
    }

    fun getContacts(): List<Pair<ByteArray, Int>> {
        val friendNumbers = tox.friendList
        Log.e("Tox", "Loading ${friendNumbers.size} friends")
        return List(friendNumbers.size) {
            Log.e("Tox", "${friendNumbers[it]}: ${tox.getFriendPublicKey(friendNumbers[it]).byteArrayToHex()}")
            Pair(tox.getFriendPublicKey(friendNumbers[it]), friendNumbers[it])
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

    fun save(destination: String) {
        val fileName = this.getPublicKey() + ".tox"

        val saveFile = File("$destination/$fileName")
        if (!saveFile.exists()) {
            saveFile.createNewFile()
        }

        Log.e("ToxCore", "Saving profile to $saveFile")

        saveFile.writeBytes(tox.savedata)
    }

    fun acceptFriendRequest(publicKey: String) {
        tox.addFriendNorequest(publicKey.hexToByteArray())
        updateContactMapping()
    }
}
