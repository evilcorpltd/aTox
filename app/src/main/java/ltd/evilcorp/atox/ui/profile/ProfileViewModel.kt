package ltd.evilcorp.atox.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import im.tox.tox4j.core.options.SaveDataOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.di.ToxThreadFactory
import ltd.evilcorp.atox.repository.UserRepository
import ltd.evilcorp.atox.tox.ToxThread
import ltd.evilcorp.atox.vo.User
import java.io.File
import javax.inject.Inject

private fun loadToxSave(saveFile: File): ByteArray? {
    if (!saveFile.exists()) {
        return null
    }

    return saveFile.readBytes()
}

class ProfileViewModel @Inject constructor(
    private val context: Context,
    private val toxThreadFactory: ToxThreadFactory,
    private val userRepository: UserRepository
) : ViewModel() {
    fun startToxThread(save: ByteArray? = null) {
        val saveOptions: SaveDataOptions = save?.let { SaveDataOptions.ToxSave(save) } ?: SaveDataOptions.`None$`()
        App.toxThread = toxThreadFactory.create(context.filesDir.toString(), saveOptions)
    }

    fun tryLoadToxSave(): ByteArray? {
        val save: File? = context.filesDir.walk().find { it.extension == "tox" && it.isFile }
        return if (save == null) {
            null
        } else {
            loadToxSave(save)
        }
    }

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
}
