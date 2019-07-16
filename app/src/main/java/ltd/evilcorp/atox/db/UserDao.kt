package ltd.evilcorp.atox.db

import androidx.lifecycle.LiveData
import androidx.room.*
import ltd.evilcorp.atox.vo.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun save(user: User)

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)

    @Query("SELECT COUNT(*) FROM users WHERE public_key = :publicKey")
    fun exists(publicKey: String): Boolean

    @Query("SELECT * FROM users WHERE public_key = :publicKey")
    fun load(publicKey: String): LiveData<User>
}
