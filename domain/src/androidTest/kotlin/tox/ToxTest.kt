// SPDX-FileCopyrightText: 2020-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToxTest {
    @ExperimentalCoroutinesApi
    @Test
    fun quitting_does_not_crash() = runTest {
        repeat(10) {
            val tox = Tox(
                TestScope(),
                mockk(relaxUnitFun = true),
                mockk(relaxUnitFun = true),
                mockk(relaxUnitFun = true),
                mockk(),
            ).apply { isBootstrapNeeded = false }
            tox.start(SaveOptions(null, false, ProxyType.None, "", 0), null, ToxEventListener(), ToxAvEventListener())
            delay(25)
            tox.stop()
        }
    }
}
