package ltd.evilcorp.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.User

@Database(
    entities = [Contact::class, FriendRequest::class, Message::class, User::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    internal abstract fun contactDao(): ContactDao
    internal abstract fun friendRequestDao(): FriendRequestDao
    internal abstract fun messageDao(): MessageDao
    internal abstract fun userDao(): UserDao
}
