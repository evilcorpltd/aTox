// SPDX-FileCopyrightText: 2022-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.Test
import ltd.evilcorp.core.vo.PublicKey
import org.junit.runner.RunWith

private val pk = PublicKey("123")

@RunWith(AndroidJUnit4::class)
class AvatarFactoryTest {
    @Test
    fun emptyName() {
        AvatarFactory.create(
            InstrumentationRegistry.getInstrumentation().targetContext.resources,
            name = "",
            pk = pk,
        )
    }

    @Test
    fun nameEndingInSpace() {
        AvatarFactory.create(
            InstrumentationRegistry.getInstrumentation().targetContext.resources,
            name = "a ",
            pk = pk,
        )
    }
}
