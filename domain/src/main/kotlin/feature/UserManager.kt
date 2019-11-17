package ltd.evilcorp.domain.feature

import android.content.Context
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox
import javax.inject.Inject

class UserManager @Inject constructor(
    private val context: Context,
    private val userRepository: UserRepository,
    private val tox: Tox
) : CoroutineScope by GlobalScope {
    fun get(publicKey: PublicKey) = userRepository.get(publicKey.string())

    fun create(publicKey: PublicKey, name: String, password: String) = launch {
        userRepository.add(User(publicKey = publicKey.string(), name = name, password = password))
        tox.setName(name)
    }

    fun verifyExists(publicKey: PublicKey) = launch {
        val name = tox.getName().await()
        val statusMessage = tox.getStatusMessage().await()
        val user = User(publicKey.string(), name, statusMessage)

        if (!userRepository.exists(publicKey.string())) {
            userRepository.add(user)
        } else {
            userRepository.update(user)
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString("name", name)
            .putString("status_message", statusMessage).apply()

        userRepository.update(user)
    }

    fun setName(name: String) = launch {
        tox.setName(name)
        userRepository.updateName(tox.publicKey.string(), name)
    }

    fun setStatusMessage(statusMessage: String) = launch {
        tox.setStatusMessage(statusMessage)
        userRepository.updateStatusMessage(tox.publicKey.string(), statusMessage)
    }
}
