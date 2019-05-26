package ltd.evilcorp.atox.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxUserStatus
import ltd.evilcorp.atox.vo.Contact

private class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun toStatus(status: Int): ToxUserStatus = ToxUserStatus.values()[status]

        @TypeConverter
        @JvmStatic
        fun fromStatus(status: ToxUserStatus): Int = status.ordinal

        @TypeConverter
        @JvmStatic
        fun toConnection(connection: Int): ToxConnection = ToxConnection.values()[connection]

        @TypeConverter
        @JvmStatic
        fun fromConnection(connection: ToxConnection): Int = connection.ordinal
    }
}

@Database(entities = [Contact::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
