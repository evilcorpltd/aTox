// SPDX-FileCopyrightText: 2020-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ltd.evilcorp.core.db.Database
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.ConnectionStatus
import org.junit.runner.RunWith

class FakeBootstrapNodeRegistry(val nodes: List<BootstrapNode> = listOf()) : BootstrapNodeRegistry {
    override fun get(n: Int): List<BootstrapNode> = nodes.take(n)
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
                this,
                contactRepository,
                userRepository,
                FakeSaveManager(),
                FakeBootstrapNodeRegistry(),
            ).apply { isBootstrapNeeded = false }
            tox.start(SaveOptions(null, false, ProxyType.None, "", 0), null, ToxEventListener(), ToxAvEventListener())
            advanceTimeBy(25.milliseconds)
            tox.stop()
            advanceUntilIdle()
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun bootstrapping_against_a_live_node_works() = runTest {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val db = Room.inMemoryDatabaseBuilder(instrumentation.context, Database::class.java).build()
        val userRepository = UserRepository(db.userDao())
        val contactRepository = ContactRepository(db.contactDao())

        var connected = false
        val eventListener = ToxEventListener().apply {
            selfConnectionStatusHandler = { status ->
                connected = status != ConnectionStatus.None
            }
        }

        val tox = Tox(
            this,
            contactRepository,
            userRepository,
            FakeSaveManager(),
            FakeBootstrapNodeRegistry(
                listOf(
                    BootstrapNode(
                        "tox.abilinski.com",
                        33445,
                        PublicKey("10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E"),
                    ),
                ),
            ),
        )
        tox.start(SaveOptions(null, false, ProxyType.None, "", 0), null, eventListener, ToxAvEventListener())

        while (!connected) {
            advanceTimeBy(100.milliseconds)
        }

        tox.stop()
        advanceUntilIdle()
    }
}
