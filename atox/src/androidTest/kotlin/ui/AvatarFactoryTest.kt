// SPDX-FileCopyrightText: 2022 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AvatarFactoryTest {
    @Test
    fun emptyName() {
        AvatarFactory.create(
            InstrumentationRegistry.getInstrumentation().targetContext.resources,
            name = "",
            publicKey = "123",
        )
    }

    @Test
    fun nameEndingInSpace() {
        AvatarFactory.create(
            InstrumentationRegistry.getInstrumentation().targetContext.resources,
            name = "a ",
            publicKey = "123",
        )
    }
}
