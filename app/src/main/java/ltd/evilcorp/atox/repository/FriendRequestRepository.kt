package ltd.evilcorp.atox.repository

import androidx.lifecycle.LiveData
import ltd.evilcorp.atox.db.FriendRequestDao
import ltd.evilcorp.atox.vo.FriendRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRequestRepository @Inject constructor(
    private val friendRequestDao: FriendRequestDao
) {
    fun add(friendRequest: FriendRequest) {
        friendRequestDao.save(friendRequest)
    }

    fun delete(friendRequest: FriendRequest) {
        friendRequestDao.delete(friendRequest)
    }

    fun getAll(): LiveData<List<FriendRequest>> {
        return friendRequestDao.loadAll()
    }
}
