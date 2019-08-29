package ltd.evilcorp.atox.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import im.tox.tox4j.core.exceptions.ToxNewException
import ltd.evilcorp.atox.feature.UserManager
import ltd.evilcorp.atox.tox.*
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val context: Context,
    private val saveManager: SaveManager,
    private val userManager: UserManager,
    private val tox: Tox,
    private val eventListener: ToxEventListener
) : ViewModel() {
    val publicKey: PublicKey by lazy { tox.publicKey }

    fun startToxThread(save: ByteArray? = null): Boolean = try {
        tox.start(SaveOptions(save), eventListener)
        true
    } catch (e: ToxNewException) {
        Log.e("ProfileViewModel", e.message)
        false
    }

    fun tryLoadToxSave(): ByteArray? = saveManager.run { list().firstOrNull()?.let { load(PublicKey(it)) } }
    fun tryImportToxSave(uri: Uri): ByteArray? = context.contentResolver.openInputStream(uri)?.readBytes()

    fun createUser(publicKey: PublicKey, name: String, password: String) =
        userManager.create(publicKey, name, password)

    fun verifyUserExists(publicKey: PublicKey) = userManager.verifyExists(publicKey)
}
