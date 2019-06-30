package ltd.evilcorp.atox.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ltd.evilcorp.atox.db.ContactDao
import ltd.evilcorp.atox.db.Database
import ltd.evilcorp.atox.db.FriendRequestDao
import ltd.evilcorp.atox.db.MessageDao
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(application: Application): Database {
        return Room.databaseBuilder(application, Database::class.java, "contact_db")
            .fallbackToDestructiveMigration() // TODO(robinlinden): Delete this.
            .build()
    }

    @Singleton
    @Provides
    fun provideContactDao(db: Database): ContactDao {
        return db.contactDao()
    }

    @Singleton
    @Provides
    fun provideFriendRequestDao(db: Database): FriendRequestDao {
        return db.friendRequestDao()
    }

    @Singleton
    @Provides
    fun provideMessageDao(db: Database): MessageDao {
        return db.messageDao()
    }
}
