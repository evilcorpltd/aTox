package ltd.evilcorp.atox.repository

import androidx.lifecycle.LiveData
import ltd.evilcorp.atox.db.UserDao
import ltd.evilcorp.atox.vo.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun exists(publicKey: String): Boolean {
        return userDao.exists(publicKey)
    }

    fun add(user: User) {
        userDao.save(user)
    }

    fun update(user: User) {
        userDao.update(user)
    }

    fun get(publicKey: String): LiveData<User> {
        return userDao.load(publicKey)
    }
}
