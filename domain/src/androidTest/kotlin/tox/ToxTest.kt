// SPDX-FileCopyrightText: 2020-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import java.lang.Thread.sleep
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToxTest {
    @Test
    fun quitting_does_not_crash() {
        repeat(10) {
            val tox = Tox(
                mockk(relaxUnitFun = true),
                mockk(relaxUnitFun = true),
                mockk(relaxUnitFun = true),
                mockk(),
            ).apply { isBootstrapNeeded = false }
            tox.start(SaveOptions(null, false, ProxyType.None, "", 0), null, ToxEventListener(), ToxAvEventListener())
            sleep(25)
            tox.stop()
        }
    }
}
