package ltd.evilcorp.core.repository

import androidx.lifecycle.LiveData
import ltd.evilcorp.core.db.UserDao
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject internal constructor(
    private val userDao: UserDao
) {
    fun exists(publicKey: String): Boolean =
        userDao.exists(publicKey)

    fun add(user: User) =
        userDao.save(user)

    fun update(user: User) =
        userDao.update(user)

    fun get(publicKey: String): LiveData<User> =
        userDao.load(publicKey)

    fun updateConnection(publicKey: String, connectionStatus: ConnectionStatus) =
        userDao.updateConnection(publicKey, connectionStatus)
}
