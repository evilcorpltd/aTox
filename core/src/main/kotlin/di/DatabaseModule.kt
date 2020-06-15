package ltd.evilcorp.core.di

import android.content.Context
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
    fun provideDatabase(appContext: Context): Database =
        Room.databaseBuilder(appContext, Database::class.java, "core_db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
}

@Suppress("unused")
@Module
class DaoModule {
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
