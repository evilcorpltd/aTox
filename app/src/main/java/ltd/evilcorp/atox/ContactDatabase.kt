package ltd.evilcorp.atox

import android.content.Context
import androidx.room.*
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxUserStatus

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

@Database(entities = [Contact::class], version = 1)
@TypeConverters(Converters::class)
abstract class ContactDatabase : RoomDatabase() {
    companion object {
        private var instance: ContactDatabase? = null

        // TODO(robinlinden): Nicer database creation/access.
        fun instance(context: Context? = null): ContactDatabase {
            if (instance != null) {
                return instance!!
            }

            // TODO(robinlinden): Look into and maybe remove allowMainThreadQueries().
            instance =
                Room.databaseBuilder(context!!.applicationContext, ContactDatabase::class.java, "contact_database")
                    .allowMainThreadQueries()
                    .build()
            return instance!!
        }
    }

    abstract fun contactDao(): ContactDao
}
