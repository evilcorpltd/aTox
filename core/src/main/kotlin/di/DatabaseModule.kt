// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import ltd.evilcorp.core.db.ALL_MIGRATIONS
import ltd.evilcorp.core.db.ContactDao
import ltd.evilcorp.core.db.Database
import ltd.evilcorp.core.db.FileTransferDao
import ltd.evilcorp.core.db.FriendRequestDao
import ltd.evilcorp.core.db.MessageDao
import ltd.evilcorp.core.db.UserDao

@Suppress("unused")
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(appContext: Context): Database =
        Room.databaseBuilder(appContext, Database::class.java, "core_db")
            .addMigrations(*ALL_MIGRATIONS)
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
