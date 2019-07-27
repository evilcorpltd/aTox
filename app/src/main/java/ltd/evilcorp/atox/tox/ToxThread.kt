package ltd.evilcorp.atox.tox

import kotlinx.coroutines.*
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FriendRequestRepository
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.User

@ObsoleteCoroutinesApi
class ToxThread(
    private val tox: Tox,
    private val contactRepository: ContactRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val userRepository: UserRepository
) : CoroutineScope by GlobalScope + newSingleThreadContext("ToxThread") {
    val toxId = tox.getToxId()
    val publicKey = tox.getPublicKey()

    init {
        fun loadSelf() = launch {
            userRepository.update(User(publicKey, tox.getName(), tox.getStatusMessage()))
        }

        fun loadContacts() = launch {
            contactRepository.resetTransientData()

            for ((publicKey, _) in tox.getContacts()) {
                if (!contactRepository.exists(publicKey)) {
                    contactRepository.add(Contact(publicKey))
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

    fun acceptFriendRequest(publicKey: String) = launch {
        tox.acceptFriendRequest(publicKey)
        save()
        contactRepository.add(Contact(publicKey))
        friendRequestRepository.delete(FriendRequest(publicKey))
    }

    fun setName(name: String) = launch {
        tox.setName(name)
        save()
    }

    fun addContact(toxId: String, message: String) = launch {
        tox.addContact(toxId, message)
        save()
        contactRepository.add(Contact(toxId.dropLast(12)))
    }

    fun deleteContact(publicKey: String) = launch {
        tox.deleteContact(publicKey)
        save()
        contactRepository.delete(Contact(publicKey))
    }

    fun sendMessage(publicKey: String, message: String) = launch {
        tox.sendMessage(publicKey, message)
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
