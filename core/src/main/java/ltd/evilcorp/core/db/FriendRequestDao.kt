package ltd.evilcorp.core.db

import androidx.lifecycle.LiveData
import androidx.room.*
import ltd.evilcorp.core.vo.FriendRequest

@Dao
interface FriendRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(friendRequest: FriendRequest)

    @Delete
    fun delete(friendRequest: FriendRequest)

    @Query("SELECT * FROM friend_requests")
    fun loadAll(): LiveData<List<FriendRequest>>
}
