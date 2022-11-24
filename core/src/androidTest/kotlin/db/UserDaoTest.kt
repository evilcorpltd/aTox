// SPDX-FileCopyrightText: 2021-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.fail

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    private val db =
        Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, Database::class.java)
            .allowMainThreadQueries()
            .build()
    private val dao = db.userDao()

    private val first = User(
        publicKey = "1234",
        name = "name",
        statusMessage = "status",
        status = UserStatus.Away,
        connectionStatus = ConnectionStatus.UDP,
        password = "password",
    )

    @BeforeTest
    fun clearDb() = db.clearAllTables()

    @Test
    fun save_and_load() = runTest {
        dao.save(first)
        assertEquals(first, dao.load(first.publicKey).first())
    }

    @Test
    fun update() = runTest {
        dao.save(first)
        dao.update(first.copy(name = "new name"))
        assertNotEquals(first, dao.load(first.publicKey).first())
        assertEquals(first.copy(name = "new name"), dao.load(first.publicKey).first())
    }

    @Test
    fun exists() = runTest {
        assertFalse(dao.exists(first.publicKey))
        dao.save(first)
        assertTrue(dao.exists(first.publicKey))
    }

    @Test
    fun cant_replace_user_with_save() = runTest {
        dao.save(first)
        try {
            dao.save(first.copy(name = "new name"))
            fail()
        } catch (_: Exception) {
        }
    }

    @Test
    fun update_name() = runTest {
        dao.save(first)
        dao.updateName(first.publicKey, "new name")
        assertNotEquals(first, dao.load(first.publicKey).first())
        assertEquals(first.copy(name = "new name"), dao.load(first.publicKey).first())
    }

    @Test
    fun update_status_message() = runTest {
        dao.save(first)
        dao.updateStatusMessage(first.publicKey, "new status")
        assertNotEquals(first, dao.load(first.publicKey).first())
        assertEquals(first.copy(statusMessage = "new status"), dao.load(first.publicKey).first())
    }

    @Test
    fun update_connection() = runTest {
        dao.save(first)
        dao.updateConnection(first.publicKey, ConnectionStatus.TCP)
        assertNotEquals(first, dao.load(first.publicKey).first())
        assertEquals(first.copy(connectionStatus = ConnectionStatus.TCP), dao.load(first.publicKey).first())
    }

    @Test
    fun update_status() = runTest {
        dao.save(first)
        dao.updateStatus(first.publicKey, UserStatus.Busy)
        assertNotEquals(first, dao.load(first.publicKey).first())
        assertEquals(first.copy(status = UserStatus.Busy), dao.load(first.publicKey).first())
    }
}
