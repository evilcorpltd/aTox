package ltd.evilcorp.core.repository

import androidx.lifecycle.LiveData
import ltd.evilcorp.core.db.FriendRequestDao
import ltd.evilcorp.core.vo.FriendRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRequestRepository @Inject internal constructor(
    private val friendRequestDao: FriendRequestDao
) {
    fun add(friendRequest: FriendRequest) =
        friendRequestDao.save(friendRequest)

    fun delete(friendRequest: FriendRequest) =
        friendRequestDao.delete(friendRequest)

    fun getAll(): LiveData<List<FriendRequest>> =
        friendRequestDao.loadAll()
}
