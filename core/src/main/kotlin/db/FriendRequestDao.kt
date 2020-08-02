package ltd.evilcorp.core.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ltd.evilcorp.core.vo.FriendRequest

@Dao
internal interface FriendRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(friendRequest: FriendRequest)

    @Delete
    fun delete(friendRequest: FriendRequest)

    @Query("SELECT * FROM friend_requests")
    fun loadAll(): LiveData<List<FriendRequest>>

    @Query("SELECT * FROM friend_requests WHERE public_key == :publicKey")
    fun load(publicKey: String): LiveData<FriendRequest>
}
