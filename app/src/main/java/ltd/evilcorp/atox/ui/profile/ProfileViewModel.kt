package ltd.evilcorp.atox.ui.profile

import android.content.Context
import android.net.Uri
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.ViewModel
import im.tox.tox4j.core.exceptions.ToxNewException
import im.tox.tox4j.core.options.SaveDataOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.di.ToxThreadFactory
import ltd.evilcorp.atox.repository.UserRepository
import ltd.evilcorp.atox.tox.ToxThread
import ltd.evilcorp.atox.vo.User
import java.io.File
import javax.inject.Inject

private fun loadToxSave(saveFile: File): ByteArray? =
    if (saveFile.exists()) {
        saveFile.readBytes()
    } else {
        null
    }

class ProfileViewModel @Inject constructor(
    private val context: Context,
    private val toxThreadFactory: ToxThreadFactory,
    private val userRepository: UserRepository
) : ViewModel() {
    fun startToxThread(save: ByteArray? = null): Boolean = try {
        val saveOptions: SaveDataOptions = save?.let { SaveDataOptions.ToxSave(save) } ?: SaveDataOptions.`None$`()
        App.toxThread = toxThreadFactory.create(saveOptions)
        setActiveUser(App.toxThread.publicKey)
        true
    } catch (e: ToxNewException) {
        Log.e("ProfileViewModel", e.message)
        false
    }

    fun tryLoadToxSave(): ByteArray? =
        context.filesDir.walk().find { it.extension == "tox" && it.isFile }?.let { save ->
            loadToxSave(save)
        }

    fun tryImportToxSave(uri: Uri): ByteArray? = context.contentResolver.openInputStream(uri)?.readBytes()

    fun createUser(publicKey: String, name: String, password: String) {
        GlobalScope.launch {
            userRepository.add(User(publicKey = publicKey, name = name, password = password))
        }

        with(App.toxThread.handler) {
            sendMessage(obtainMessage(ToxThread.msgSetName, name))
        }
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
