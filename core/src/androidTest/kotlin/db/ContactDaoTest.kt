// SPDX-FileCopyrightText: 2020-2022 aTox contributors
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
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.UserStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ContactDaoTest {
    private val db =
        Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, Database::class.java)
            .allowMainThreadQueries()
            .build()
    private val dao = db.contactDao()

    private val first = Contact(
        publicKey = "1234",
        name = "name",
        statusMessage = "status",
        lastMessage = 5,
        status = UserStatus.Away,
        connectionStatus = ConnectionStatus.UDP,
        typing = true,
        avatarUri = "uri",
        hasUnreadMessages = true,
        draftMessage = "i made this",
    )

    private val second = first.copy(publicKey = "5678")

    @Before
    fun clearDb() {
        db.clearAllTables()
    }

    @Test
    fun save_and_load() = runTest {
        assertEquals(0, dao.loadAll().first().size)
        dao.save(first)
        assertEquals(first, dao.load(first.publicKey).first())
    }

    @Test
    fun delete() = runTest {
        dao.save(first)
        dao.save(second)
        dao.delete(first)
        assertEquals(1, dao.loadAll().first().size)
        assertEquals(second, dao.loadAll().first()[0])
    }

    @Test
    fun exists() = runTest {
        assertFalse(dao.exists(first.publicKey))
        dao.save(first)
        assertTrue(dao.exists(first.publicKey))
        assertFalse(dao.exists(second.publicKey))
        dao.save(second)
        assertTrue(dao.exists(second.publicKey))
    }

    @Test
    fun overwrite_with_save() = runTest {
        dao.save(first)
        assertEquals(1, dao.loadAll().first().size)
        dao.save(first)
        assertEquals(1, dao.loadAll().first().size)
    }

    @Test
    fun save_multiple_contacts() = runTest {
        dao.save(first)
        assertEquals(1, dao.loadAll().first().size)
        dao.save(second)
        assertEquals(2, dao.loadAll().first().size)
        assertEquals(first, dao.load(first.publicKey).first())
        assertEquals(second, dao.load(second.publicKey).first())
    }

    @Test
    fun reset_transient_data() = runTest {
        dao.save(first)
        dao.resetTransientData()
        assertNotEquals(first, dao.load(first.publicKey).first())
        assertEquals(
            first.copy(typing = false, connectionStatus = ConnectionStatus.None),
            dao.load(first.publicKey).first(),
        )
    }

    @Test
    fun setters() = runTest {
        dao.save(second)
        dao.setName(second.publicKey, first.name)
        dao.setStatusMessage(second.publicKey, first.statusMessage)
        dao.setLastMessage(second.publicKey, first.lastMessage)
        dao.setUserStatus(second.publicKey, first.status)
        dao.setConnectionStatus(second.publicKey, first.connectionStatus)
        dao.setTyping(second.publicKey, first.typing)
        dao.setAvatarUri(second.publicKey, first.avatarUri)
        dao.setHasUnreadMessages(second.publicKey, first.hasUnreadMessages)
        dao.setDraftMessage(second.publicKey, first.draftMessage)
        assertEquals(first, dao.load(second.publicKey).first().copy(publicKey = first.publicKey))
    }

    @Test
    fun update() = runTest {
        dao.save(first)
        dao.update(first.copy(name = "new name"))
        assertNotEquals(first, dao.load(first.publicKey).first())
        assertEquals(first.copy(name = "new name"), dao.load(first.publicKey).first())
    }
}
