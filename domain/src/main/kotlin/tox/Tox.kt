package ltd.evilcorp.domain.tox

import android.util.Log
import im.tox.tox4j.core.exceptions.ToxBootstrapException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FileKind
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.UserStatus

private const val TAG = "Tox"

@ObsoleteCoroutinesApi
@Singleton
class Tox @Inject constructor(
    private val contactRepository: ContactRepository,
    private val userRepository: UserRepository,
    private val saveManager: SaveManager,
    private val nodeRegistry: BootstrapNodeRegistry,
) : CoroutineScope by GlobalScope + newSingleThreadContext("Tox") {
    val toxId: ToxID get() = tox.getToxId()
    val publicKey: PublicKey by lazy { tox.getPublicKey() }
    var nospam: Int
        get() = tox.getNospam()
        set(value) = tox.setNospam(value)

    var started = false
    var isBootstrapNeeded = true

    private var running = false
    private var toxAvRunning = false

    private lateinit var tox: ToxWrapper

    fun start(saveOption: SaveOptions, listener: ToxEventListener, avListener: ToxAvEventListener) {
        tox = ToxWrapper(listener, avListener, saveOption)
        started = true

        fun loadContacts() = launch {
            contactRepository.resetTransientData()

            for ((publicKey, _) in tox.getContacts()) {
                if (!contactRepository.exists(publicKey.string())) {
                    contactRepository.add(Contact(publicKey.string()))
                }
            }
        }

        fun iterateForeverAv() = launch {
            toxAvRunning = true
            while (running) {
                tox.iterateAv()
                delay(tox.iterationIntervalAv())
            }
            toxAvRunning = false
        }

        fun iterateForever() = launch {
            running = true
            userRepository.updateConnection(publicKey.string(), ConnectionStatus.None)
            while (running || toxAvRunning) {
                if (isBootstrapNeeded) {
                    try {
                        bootstrap()
                        isBootstrapNeeded = false
                    } catch (e: ToxBootstrapException) {
                        Log.e(TAG, e.toString())
                    }
                }
                tox.iterate()
                delay(tox.iterationInterval())
            }
            started = false
        }

        save()
        loadContacts()
        iterateForever()
        iterateForeverAv()
    }

    fun stop() = launch {
        running = false
        while (started) delay(10)
        save()
        tox.stop()
    }

    private fun save() = runBlocking {
        saveManager.save(publicKey, tox.getSaveData())
    }

    fun acceptFriendRequest(publicKey: PublicKey) = launch {
        tox.acceptFriendRequest(publicKey)
        save()
    }

    fun startFileTransfer(pk: PublicKey, fileNumber: Int) = launch {
        Log.i(TAG, "Starting file transfer $fileNumber from ${pk.fingerprint()}")
        tox.startFileTransfer(pk, fileNumber)
    }

    fun stopFileTransfer(pk: PublicKey, fileNumber: Int) = launch {
        Log.i(TAG, "Stopping file transfer $fileNumber from ${pk.fingerprint()}")
        tox.stopFileTransfer(pk, fileNumber)
    }

    fun sendFile(pk: PublicKey, fileKind: FileKind, fileSize: Long, fileName: String) = async {
        tox.sendFile(pk, fileKind, fileSize, fileName)
    }

    fun sendFileChunk(pk: PublicKey, fileNo: Int, pos: Long, data: ByteArray) = launch {
        tox.sendFileChunk(pk, fileNo, pos, data)
    }

    fun getName() = async { tox.getName() }
    fun setName(name: String) = launch {
        tox.setName(name)
        save()
    }

    fun getStatusMessage() = async { tox.getStatusMessage() }
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

    fun sendMessage(publicKey: PublicKey, message: String, type: MessageType) = async {
        tox.sendMessage(publicKey, message, type)
    }

    fun getSaveData() = async {
        tox.getSaveData()
    }

    private fun bootstrap() {
        nodeRegistry.get(4).forEach { node ->
            Log.i(TAG, "Bootstrapping from $node")
            tox.bootstrap(node.address, node.port, node.publicKey.bytes())
        }
    }

    fun setTyping(publicKey: PublicKey, typing: Boolean) = launch {
        tox.setTyping(publicKey, typing)
    }

    fun setStatus(status: UserStatus) = launch { tox.setStatus(status) }
    fun getStatus() = async { tox.getStatus() }

    // ToxAv, probably move these.
    fun startCall(pk: PublicKey) = tox.startCall(pk)
    fun endCall(pk: PublicKey) = tox.endCall(pk)
    fun sendAudio(pk: PublicKey, pcm: ShortArray, channels: Int, samplingRate: Int) =
        tox.sendAudio(pk, pcm, channels, samplingRate)
}
