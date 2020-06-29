package ltd.evilcorp.domain.feature

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox

class UserManager @Inject constructor(
    private val userRepository: UserRepository,
    private val tox: Tox
) : CoroutineScope by GlobalScope {
    fun get(publicKey: PublicKey) = userRepository.get(publicKey.string())

    fun create(user: User) = launch {
        userRepository.add(user)
        tox.setName(user.name)
        tox.setStatusMessage(user.statusMessage)
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

    fun setStatus(status: UserStatus) = launch {
        tox.setStatus(status)
        userRepository.updateStatus(tox.publicKey.string(), status)
    }
}
