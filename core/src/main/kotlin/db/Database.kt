// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FileTransfer
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.User

@Database(
    entities = [Contact::class, FileTransfer::class, FriendRequest::class, Message::class, User::class],
    version = 6
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun fileTransferDao(): FileTransferDao
    abstract fun friendRequestDao(): FriendRequestDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
}
