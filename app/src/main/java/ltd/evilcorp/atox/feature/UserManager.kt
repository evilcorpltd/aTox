package ltd.evilcorp.atox.feature

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.tox.PublicKey
import ltd.evilcorp.atox.tox.Tox
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.User
import javax.inject.Inject

class UserManager @Inject constructor(
    private val userRepository: UserRepository,
    private val tox: Tox
) : CoroutineScope by GlobalScope {
    fun get(publicKey: PublicKey) = userRepository.get(publicKey.string())

    fun create(publicKey: PublicKey, name: String, password: String) = launch {
        userRepository.add(User(publicKey = publicKey.string(), name = name, password = password))
        tox.setName(name)
    }

    fun verifyExists(publicKey: PublicKey) = launch {
        if (!userRepository.exists(publicKey.string())) {
            userRepository.add(User(publicKey.string()))
        }
    }
}
