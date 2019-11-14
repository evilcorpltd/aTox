package ltd.evilcorp.core.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ltd.evilcorp.core.db.*
import javax.inject.Singleton

@Suppress("unused")
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(application: Application): Database =
        Room.databaseBuilder(application, Database::class.java, "core_db")
            .fallbackToDestructiveMigration() // TODO(robinlinden): Delete this.
            .build()

    @Singleton
    @Provides
    internal fun provideContactDao(db: Database): ContactDao = db.contactDao()

    @Singleton
    @Provides
    internal fun provideFileTransferDao(db: Database): FileTransferDao = db.fileTransferDao()

    @Singleton
    @Provides
    internal fun provideFriendRequestDao(db: Database): FriendRequestDao = db.friendRequestDao()

    @Singleton
    @Provides
    internal fun provideMessageDao(db: Database): MessageDao = db.messageDao()

    @Singleton
    @Provides
    internal fun provideUserDao(db: Database): UserDao = db.userDao()
}
