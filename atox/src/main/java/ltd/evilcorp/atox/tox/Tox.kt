package ltd.evilcorp.atox.tox

import android.util.Log
import im.tox.tox4j.core.exceptions.ToxBootstrapException
import kotlinx.coroutines.*
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.User
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "Tox"

@ObsoleteCoroutinesApi
@Singleton
class Tox @Inject constructor(
    private val contactRepository: ContactRepository,
    private val userRepository: UserRepository,
    private val saveManager: SaveManager
) : CoroutineScope by GlobalScope + newSingleThreadContext("Tox") {
    val toxId: ToxID by lazy { tox.getToxId() }
    val publicKey: PublicKey by lazy { tox.getPublicKey() }

    var started = false

    private var running = false
    private var isBootstrapNeeded = true

    private lateinit var tox: ToxWrapper

    fun start(saveOption: SaveOptions, eventListener: ToxEventListener) {
        started = true

        tox = ToxWrapper(eventListener, saveOption)

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
            running = true
            while (running) {
                if (isBootstrapNeeded) {
                    bootstrap()
                    isBootstrapNeeded = false
                }
                iterate()
                delay(tox.iterationInterval())
            }
        }

        save()
        loadSelf()
        loadContacts()
        iterateForever()
    }

    fun stop() = launch {
        running = false
        save()
        tox.stop()
        started = false
    }

    private fun save() = runBlocking {
        saveManager.save(publicKey, tox.getSaveData())
    }

    private fun iterate() = launch {
        tox.iterate()
    }

    fun acceptFriendRequest(publicKey: PublicKey) = launch {
        tox.acceptFriendRequest(publicKey)
        save()
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
    }

    fun deleteContact(publicKey: PublicKey) = launch {
        tox.deleteContact(publicKey)
        save()
    }

    fun sendMessage(publicKey: PublicKey, message: String) = async {
        tox.sendMessage(publicKey, message)
    }

    fun getSaveData() = async {
        tox.getSaveData()
    }

    private fun bootstrap() = launch {
        // TODO(robinlinden): Read these from somewhere else.
        val nodes = listOf(
            BootstrapNode(
                "tox.verdict.gg",
                33445,
                PublicKey("1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976")
            ),
            BootstrapNode(
                "tox.kurnevsky.net",
                33445,
                PublicKey("82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23")
            ),
            BootstrapNode(
                "tox.abilinski.com",
                33445,
                PublicKey("10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E")
            )
        )

        try {
            nodes.shuffled().take(3).forEach { node ->
                tox.bootstrap(node.address, node.port, node.publicKey.bytes())
            }
        } catch (e: ToxBootstrapException) {
            Log.e(TAG, e.toString())
            isBootstrapNeeded = true
        }
    }

    fun setTyping(publicKey: PublicKey, typing: Boolean) = launch {
        tox.setTyping(publicKey, typing)
    }
}
