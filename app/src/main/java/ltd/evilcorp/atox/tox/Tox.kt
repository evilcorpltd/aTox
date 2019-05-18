package ltd.evilcorp.atox.tox

import android.util.Log
import kotlinx.coroutines.*
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FriendRequestRepository
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.User
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "Tox"

@ObsoleteCoroutinesApi
@Singleton
class Tox @Inject constructor(
    private val toxFactory: ToxWrapperFactory,
    private val contactRepository: ContactRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val userRepository: UserRepository
) : CoroutineScope by GlobalScope + newSingleThreadContext("Tox") {
    val toxId: ToxID by lazy { tox.getToxId() }
    val publicKey: PublicKey by lazy { tox.getPublicKey() }

    var started = false

    private lateinit var tox: ToxWrapper

    fun start(saveOption: SaveOptions, eventListener: ToxEventListener) {
        started = true

        tox = toxFactory.create(saveOption, eventListener)

        fun loadSelf() = launch {
            userRepository.update(User(publicKey.string(), tox.getName(), tox.getStatusMessage()))
        }

        fun loadContacts() = launch {
            contactRepository.resetTransientData()

            for ((publicKey, _) in tox.getContacts()) {
                if (!contactRepository.exists(publicKey.string())) {
                    contactRepository.add(Contact(publicKey.string()))
                }
            }
        }

        fun iterateForever() = launch {
            while (true) {
                iterate()
                delay(tox.iterationInterval())
            }
        }

        save()
        loadSelf()
        loadContacts()
        bootstrap()
        iterateForever()
    }

    private fun save() = runBlocking {
        tox.save()
    }

    private fun iterate() = launch {
        tox.iterate()
    }

    fun acceptFriendRequest(publicKey: PublicKey) = launch {
        tox.acceptFriendRequest(publicKey)
        save()
        contactRepository.add(Contact(publicKey.string()))
        friendRequestRepository.delete(FriendRequest(publicKey.string()))
    }

    fun startFileTransfer(publicKey: PublicKey, fileNumber: Int) = launch {
        Log.e(TAG, "Starting file transfer $fileNumber from $publicKey")
        tox.startFileTransfer(publicKey, fileNumber)
    }

    fun stopFileTransfer(publicKey: PublicKey, fileNumber: Int) = launch {
        Log.e(TAG, "Stopping file transfer $fileNumber from $publicKey")
        tox.stopFileTransfer(publicKey, fileNumber)
    }

    fun setName(name: String) = launch {
        tox.setName(name)
        save()
    }

    fun setStatusMessage(statusMessage: String) = launch {
        tox.setStatusMessage(statusMessage)
        save()
    }

    fun addContact(toxId: ToxID, message: String) = launch {
        tox.addContact(toxId, message)
        save()
        contactRepository.add(Contact(toxId.toPublicKey().string()))
    }

    fun setStatusMessage(status: String) {
        tox.statusMessage = status.toByteArray()
    }

    fun getStatusMessage(): String {
        return String(tox.statusMessage)
    }

    fun setStatus(status: Int) {
        when (status) {
            0 -> {
                tox.status = ToxUserStatus.NONE
            }
            1 -> {
                tox.status = ToxUserStatus.AWAY
            }
            2 -> {
                tox.status = ToxUserStatus.BUSY
            }
        }
    }

    fun deleteContact(publicKey: PublicKey) = launch {
        tox.deleteContact(publicKey)
        save()
        contactRepository.delete(Contact(publicKey.string()))
    }

    fun sendMessage(publicKey: PublicKey, message: String) = launch {
        tox.sendMessage(publicKey, message)
    }

    fun getSaveData() = async {
        tox.getSaveData()
    }

    private fun bootstrap() = launch {
        tox.bootstrap(
            "tox.verdict.gg",
            33445,
            "1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976".hexToBytes()
        )
        tox.bootstrap(
            "tox.kurnevsky.net",
            33445,
            "82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23".hexToBytes()
        )
        tox.bootstrap(
            "tox.abilinski.com",
            33445,
            "10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E".hexToBytes()
        )
    }
}
