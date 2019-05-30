package ltd.evilcorp.atox.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import ltd.evilcorp.atox.db.ContactDao
import ltd.evilcorp.atox.db.ContactDatabase
import javax.inject.Singleton

@Module
class ContactModule {
    @Singleton
    @Provides
    fun provideDatabase(application: Application): ContactDatabase {
        return Room.databaseBuilder(application, ContactDatabase::class.java, "contact_db")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration() // TODO(robinlinden): Delete this.
            .build()
    }

    @Singleton
    @Provides
    fun provideDao(db: ContactDatabase): ContactDao {
        return db.contactDao()
    }
}
