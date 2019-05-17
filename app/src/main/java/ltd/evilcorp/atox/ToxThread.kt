package ltd.evilcorp.atox

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import im.tox.tox4j.core.options.ProxyOptions
import im.tox.tox4j.core.options.SaveDataOptions
import im.tox.tox4j.core.options.ToxOptions

private const val HEX_CHARS = "0123456789ABCDEF"

private fun String.hexToByteArray(): ByteArray {
    val bytes = ByteArray(length / 2)

    for (i in 0 until length step 2) {
        bytes[i.shr(1)] = HEX_CHARS.indexOf(this[i]).shl(4).or(HEX_CHARS.indexOf(this[i + 1])).toByte()
    }

    return bytes
}

class ToxThread(saveDestination: String, saveOption: SaveDataOptions) : HandlerThread("Tox") {
    companion object {
        private const val msgIterate = 0
        const val msgSave = 1
        const val msgSetName = 2
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
