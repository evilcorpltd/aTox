// SPDX-FileCopyrightText: 2020-2022 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import ltd.evilcorp.core.db.Database
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.UserRepository
import org.junit.runner.RunWith

class FakeBootstrapNodeRegistry : BootstrapNodeRegistry {
    override fun get(n: Int): List<BootstrapNode> = listOf()
    override fun reset() {}
}

class FakeSaveManager : SaveManager {
    override fun list(): List<String> = listOf()
    override fun load(pk: PublicKey): ByteArray? = null
    override fun save(pk: PublicKey, saveData: ByteArray) {}
}

@RunWith(AndroidJUnit4::class)
class ToxTest {
    @ExperimentalCoroutinesApi
    @Test
    fun quitting_does_not_crash() = runTest {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val db = Room.inMemoryDatabaseBuilder(instrumentation.context, Database::class.java).build()
        val userRepository = UserRepository(db.userDao())
        val contactRepository = ContactRepository(db.contactDao())

        repeat(10) {
            val tox = Tox(
                TestScope(),
                contactRepository,
                userRepository,
                FakeSaveManager(),
                FakeBootstrapNodeRegistry(),
            ).apply { isBootstrapNeeded = false }
            tox.start(SaveOptions(null, false, ProxyType.None, "", 0), null, ToxEventListener(), ToxAvEventListener())
            delay(25)
            tox.stop()
        }
    }
}
