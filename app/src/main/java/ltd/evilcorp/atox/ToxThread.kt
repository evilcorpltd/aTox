package ltd.evilcorp.atox

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import im.tox.tox4j.core.options.ProxyOptions
import im.tox.tox4j.core.options.SaveDataOptions
import im.tox.tox4j.core.options.ToxOptions

class ToxThread(saveDestination: String, saveOption: SaveDataOptions) : HandlerThread("Tox") {
    companion object {
        private const val msgIterate = 0
        const val msgSave = 1
        const val msgSetName = 2
        const val msgAddContact = 3
        const val msgSendMsg = 4
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
                    true
                }
                msgSave -> {
                    Log.e("ToxThread", "Save")
                    tox.save(saveDestination, false)
                    true
                }
                msgSetName -> {
                    Log.e("ToxThread", "SetName: ${it.obj as String}")
                    tox.setName(it.obj as String)
                    true
                }
                msgAddContact -> {
                    val addContact = it.obj as MsgAddContact
                    Log.e("ToxThread", "AddContact: ${addContact.toxId} ${addContact.message}")
                    tox.addContact(addContact.toxId, addContact.message)
                    true
                }
                msgSendMsg -> {
                    Log.e("ToxThread", "Sending message to friend number: ${it.arg1}")
                    tox.sendMessage(it.arg1, it.obj.toString())
                    true
                }
                else -> {
                    Log.e("ToxThread", "Unknown message: ${it.what}")
                    false
                }
            }
        }
    }

    init {
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
