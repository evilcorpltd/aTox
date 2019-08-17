package ltd.evilcorp.atox.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import im.tox.tox4j.core.exceptions.ToxNewException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.tox.SaveManager
import ltd.evilcorp.atox.tox.SaveOptions
import ltd.evilcorp.atox.tox.ToxEventListener
import ltd.evilcorp.atox.tox.ToxThread
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.User
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val context: Context,
    private val saveManager: SaveManager,
    private val userRepository: UserRepository,
    private val tox: ToxThread,
    private val eventListener: ToxEventListener
) : ViewModel() {
    lateinit var publicKey: String

    fun startToxThread(save: ByteArray? = null): Boolean = try {
        tox.start(SaveOptions(save), eventListener)
        publicKey = tox.publicKey
        true
    } catch (e: ToxNewException) {
        Log.e("ProfileViewModel", e.message)
        false
    }

    fun tryLoadToxSave(): ByteArray? = saveManager.run { list().firstOrNull()?.let { load(it) } }
    fun tryImportToxSave(uri: Uri): ByteArray? = context.contentResolver.openInputStream(uri)?.readBytes()

    fun createUser(publicKey: String, name: String, password: String) {
        GlobalScope.launch {
            userRepository.add(User(publicKey = publicKey, name = name, password = password))
        }

        tox.setName(name)
    }

    fun verifyUserExists(publicKey: String) = GlobalScope.launch {
        if (!userRepository.exists(publicKey)) {
            userRepository.add(User(publicKey))
        }
    }
}
