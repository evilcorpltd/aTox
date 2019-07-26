package ltd.evilcorp.core.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ltd.evilcorp.core.db.*
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(application: Application): Database {
        return Room.databaseBuilder(application, Database::class.java, "core_db")
            .fallbackToDestructiveMigration() // TODO(robinlinden): Delete this.
            .build()
    }

    @Singleton
    @Provides
    internal fun provideContactDao(db: Database): ContactDao {
        return db.contactDao()
    }

    @Singleton
    @Provides
    internal fun provideFriendRequestDao(db: Database): FriendRequestDao {
        return db.friendRequestDao()
    }

    @Singleton
    @Provides
    internal fun provideMessageDao(db: Database): MessageDao {
        return db.messageDao()
    }

    @Singleton
    @Provides
    internal fun provideUserDao(db: Database): UserDao {
        return db.userDao()
    }
}
