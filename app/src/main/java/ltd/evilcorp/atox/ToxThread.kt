package ltd.evilcorp.atox

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import im.tox.tox4j.core.options.ProxyOptions
import im.tox.tox4j.core.options.SaveDataOptions
import im.tox.tox4j.core.options.ToxOptions
import ltd.evilcorp.atox.activity.MSG_CONTACTS_LOADED

class ToxThread(saveDestination: String, saveOption: SaveDataOptions, uiHandler: Handler) :
    HandlerThread("Tox") {
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
    }

    private val tox = Tox(
        ToxOptions(
            true,
            true,
            true,
            ProxyOptions.`None$`(),
            0,
            0,
            0,
            saveOption,
            true
        )
    )

    val handler: Handler by lazy {
        Handler(looper) {
            when (it.what) {
                msgIterate -> {
                    tox.iterate()
                    handler.sendEmptyMessageDelayed(msgIterate, tox.iterationInterval().toLong())
                }
                msgSave -> {
                    Log.e("ToxThread", "Save")
                    tox.save(saveDestination, false)
                }
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
                }
                msgDeleteContact -> Log.e("ToxThread", "Delete contact")
                msgAcceptContact -> Log.e("ToxThread", "Accept contact request")
                msgSendMsg -> {
                    Log.e("ToxThread", "Sending message to friend number: ${it.arg1}")
                    tox.sendMessage(it.arg1, it.obj.toString())
                }
                msgGroupCreate -> Log.e("ToxThread", "Create group")
                msgGroupLeave -> Log.e("ToxThread", "Leave group")
                msgGroupMessage -> Log.e("ToxThread", "Send group message")
                msgGroupTopic -> Log.e("ToxThread", "Set group topic")
                msgGroupInvite -> Log.e("ToxThread", "Invite group")
                msgGroupJoin -> Log.e("ToxThread", "Join group")
                else -> {
                    Log.e("ToxThread", "Unknown message: ${it.what}")
                    return@Handler false
                }
            }
            true
        }
    }

    init {
        with(uiHandler) {
            sendMessage(obtainMessage(MSG_CONTACTS_LOADED, tox.getContacts()))
        }

        start()
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

        handler.sendEmptyMessage(msgIterate)
    }
}
