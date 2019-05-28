package ltd.evilcorp.atox.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import ltd.evilcorp.atox.vo.ConnectionStatus
import ltd.evilcorp.atox.vo.Contact
import ltd.evilcorp.atox.vo.UserStatus

private class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun toStatus(status: Int): UserStatus = UserStatus.values()[status]

        @TypeConverter
        @JvmStatic
        fun fromStatus(status: UserStatus): Int = status.ordinal

        @TypeConverter
        @JvmStatic
        fun toConnection(connection: Int): ConnectionStatus = ConnectionStatus.values()[connection]

        @TypeConverter
        @JvmStatic
        fun fromConnection(connection: ConnectionStatus): Int = connection.ordinal
    }
}

@Database(entities = [Contact::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
