package ltd.evilcorp.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ltd.evilcorp.core.vo.*

@Database(
    entities = [Contact::class, FileTransfer::class, FriendRequest::class, Message::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    internal abstract fun contactDao(): ContactDao
    internal abstract fun fileTransferDao(): FileTransferDao
    internal abstract fun friendRequestDao(): FriendRequestDao
    internal abstract fun messageDao(): MessageDao
    internal abstract fun userDao(): UserDao
}
