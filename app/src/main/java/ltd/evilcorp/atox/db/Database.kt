package ltd.evilcorp.atox.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ltd.evilcorp.atox.vo.Contact
import ltd.evilcorp.atox.vo.FriendRequest
import ltd.evilcorp.atox.vo.Message
import ltd.evilcorp.atox.vo.User

@Database(
    entities = [Contact::class, FriendRequest::class, Message::class, User::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun friendRequestDao(): FriendRequestDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
}
