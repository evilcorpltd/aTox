package ltd.evilcorp.atox.tox

import android.util.Log
import im.tox.tox4j.core.exceptions.ToxNewException
import ltd.evilcorp.atox.feature.UserManager
import javax.inject.Inject

private const val TAG = "ToxStarter"

class ToxStarter @Inject constructor(
    private val saveManager: SaveManager,
    private val userManager: UserManager,
    private val tox: Tox,
    private val eventListener: ToxEventListener
) {
    fun startTox(save: ByteArray? = null): Boolean = try {
        tox.start(SaveOptions(save), eventListener)
        true
    } catch (e: ToxNewException) {
        Log.e(TAG, e.message)
        false
    }

    fun tryLoadTox(): Boolean {
        tryLoadSave()?.also { save ->
            startTox(save)
            userManager.verifyExists(tox.publicKey)
            return true
        }
        return false
    }

    private fun tryLoadSave(): ByteArray? =
        saveManager.run { list().firstOrNull()?.let { load(PublicKey(it)) } }
}
