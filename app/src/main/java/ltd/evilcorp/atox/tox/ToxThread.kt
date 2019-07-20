package ltd.evilcorp.atox.tox

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import im.tox.tox4j.core.options.SaveDataOptions
import ltd.evilcorp.atox.di.ToxFactory
import ltd.evilcorp.atox.repository.ContactRepository
import ltd.evilcorp.atox.repository.FriendRequestRepository
import ltd.evilcorp.atox.repository.UserRepository
import ltd.evilcorp.atox.vo.Contact
import ltd.evilcorp.atox.vo.FriendRequest
import ltd.evilcorp.atox.vo.User

class ToxThread(
    saveOption: SaveDataOptions,
    toxFactory: ToxFactory,
    private val contactRepository: ContactRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val userRepository: UserRepository
) : HandlerThread("Tox") {
    companion object {
        // Tox
        private const val msgIterate = 0
        private const val msgSave = 1
        const val msgShutdown = 2

        // self
        const val msgSetName = 3
        const val msgSetStatus = 4
        const val msgSetState = 5
        const val msgSetTyping = 6

        // contacts
        const val msgAddContact = 7
        const val msgDeleteContact = 8
        const val msgSendMsg = 9
        const val msgAcceptContact = 10

        // groups
        const val msgGroupCreate = 11
        const val msgGroupLeave = 12
        const val msgGroupMessage = 13
        const val msgGroupTopic = 14
        const val msgGroupInvite = 15
        const val msgGroupJoin = 16

        private const val msgLoadContacts = 17
        private const val msgLoadSelf = 18

        const val msgAcceptFriendRequest = 19
    }

    private val tox = toxFactory.create(saveOption)
    val toxId = tox.getToxId()
    val publicKey = tox.getPublicKey()

    private fun loadContacts() {
        contactRepository.resetTransientData()

        for ((publicKey, _) in tox.getContacts()) {
            if (!contactRepository.exists(publicKey)) {
                contactRepository.add(Contact(publicKey))
            }
        }
    }

    val handler: Handler by lazy {
        Handler(looper) {
            when (it.what) {
                msgIterate -> {
                    tox.iterate()
                    handler.sendEmptyMessageDelayed(msgIterate, tox.iterationInterval().toLong())
                }
                msgSave -> tox.save()
                msgShutdown -> {
                    Log.e("ToxThread", "Shutting down tox")
                    tox.kill()
                }
                msgSetName -> {
                    Log.e("ToxThread", "SetName: ${it.obj as String}")
                    tox.setName(it.obj as String)
                    handler.sendEmptyMessage(msgSave)
                }
                msgSetStatus -> Log.e("ToxThread", "Setting status")
                msgSetState -> Log.e("ToxThread", "Setting state")
                msgSetTyping -> Log.e("ToxThread", "Set typing")
                msgAddContact -> {
                    val addContact = it.obj as MsgAddContact
                    Log.e("ToxThread", "AddContact: ${addContact.toxId} ${addContact.message}")
                    tox.addContact(addContact.toxId, addContact.message)
                    handler.sendEmptyMessage(msgSave)
                    contactRepository.add(Contact(addContact.toxId.dropLast(12)))
                }
                msgDeleteContact -> {
                    val publicKey = it.obj as String
                    tox.deleteContact(publicKey)
                    contactRepository.delete(Contact(publicKey))
                    handler.sendEmptyMessage(msgSave)
                }
                msgAcceptContact -> Log.e("ToxThread", "Accept contact request")
                msgSendMsg -> {
                    val data = it.obj as MsgSendMessage
                    tox.sendMessage(data.publicKey, data.message)
                }
                msgGroupCreate -> Log.e("ToxThread", "Create group")
                msgGroupLeave -> Log.e("ToxThread", "Leave group")
                msgGroupMessage -> Log.e("ToxThread", "Send group message")
                msgGroupTopic -> Log.e("ToxThread", "Set group topic")
                msgGroupInvite -> Log.e("ToxThread", "Invite group")
                msgGroupJoin -> Log.e("ToxThread", "Join group")
                msgLoadContacts -> loadContacts()
                msgLoadSelf -> userRepository.update(User(publicKey, tox.getName()))
                msgAcceptFriendRequest -> {
                    val publicKey = it.obj as String

                    tox.acceptFriendRequest(publicKey)
                    contactRepository.add(Contact(publicKey))
                    friendRequestRepository.delete(FriendRequest(publicKey))

                    handler.sendEmptyMessage(msgSave)
                }
                else -> {
                    Log.e("ToxThread", "Unknown message: ${it.what}")
                    return@Handler false
                }
            }
            true
        }
    }

    init {
        start()
        handler.sendEmptyMessage(msgLoadSelf)
        handler.sendEmptyMessage(msgSave)
        handler.sendEmptyMessage(msgLoadContacts)
        handler.sendEmptyMessage(msgIterate)
    }

    override fun onLooperPrepared() {
        tox.bootstrap(
            "tox.verdict.gg",
            33445,
            "1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976".hexToByteArray()
        )
        tox.bootstrap(
            "tox.kurnevsky.net",
            33445,
            "82EF82BA33445A1F91A7DB27189ECFC0C013E06E3DA71F588ED692BED625EC23".hexToByteArray()
        )
        tox.bootstrap(
            "tox.abilinski.com",
            33445,
            "10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E".hexToByteArray()
        )
    }
}
