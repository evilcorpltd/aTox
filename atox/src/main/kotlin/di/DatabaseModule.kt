// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.di

import androidx.room.Room
import ltd.evilcorp.core.db.ALL_MIGRATIONS
import ltd.evilcorp.core.db.Database
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

fun databaseModule() = DI.Module(name = "DatabaseModule") {
    bind {
        singleton {
            Room.databaseBuilder(instance(), Database::class.java, "core_db")
                .addMigrations(*ALL_MIGRATIONS)
                .build()
        }
    }
}

fun daoModule() = DI.Module(name = "DaoModule") {
    bind { singleton { instance<Database>().contactDao() } }
    bind { singleton { instance<Database>().fileTransferDao() } }
    bind { singleton { instance<Database>().friendRequestDao() } }
    bind { singleton { instance<Database>().messageDao() } }
    bind { singleton { instance<Database>().userDao() } }
}
