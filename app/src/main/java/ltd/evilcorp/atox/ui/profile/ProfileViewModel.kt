package ltd.evilcorp.atox.ui.profile

import android.content.Context
import android.net.Uri
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.ViewModel
import im.tox.tox4j.core.exceptions.ToxNewException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.di.ToxThreadFactory
import ltd.evilcorp.atox.tox.SaveManager
import ltd.evilcorp.atox.tox.SaveOptions
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.User
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val context: Context,
    private val saveManager: SaveManager,
    private val toxThreadFactory: ToxThreadFactory,
    private val userRepository: UserRepository
) : ViewModel() {
    fun startToxThread(save: ByteArray? = null): Boolean = try {
        App.toxThread = toxThreadFactory.create(SaveOptions(save))
        setActiveUser(App.toxThread.publicKey)
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

        App.toxThread.setName(name)
    }

    fun verifyUserExists(publicKey: String) = GlobalScope.launch {
        if (!userRepository.exists(publicKey)) {
            userRepository.add(User(publicKey))
        }
    }

    private fun setActiveUser(publicKey: String) =
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(
            context.getString(R.string.pref_active_user),
            publicKey
        ).apply()
}
