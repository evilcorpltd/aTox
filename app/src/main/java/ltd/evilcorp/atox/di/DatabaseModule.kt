package ltd.evilcorp.atox.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ltd.evilcorp.atox.db.ContactDao
import ltd.evilcorp.atox.db.Database
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(application: Application): Database {
        return Room.databaseBuilder(application, Database::class.java, "contact_db")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration() // TODO(robinlinden): Delete this.
            .build()
    }

    @Singleton
    @Provides
    fun provideDao(db: Database): ContactDao {
        return db.contactDao()
    }
}
