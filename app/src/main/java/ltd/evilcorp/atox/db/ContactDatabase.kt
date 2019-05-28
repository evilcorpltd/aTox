package ltd.evilcorp.atox.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ltd.evilcorp.atox.vo.Contact

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
