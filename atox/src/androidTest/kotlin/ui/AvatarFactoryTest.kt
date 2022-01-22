// SPDX-FileCopyrightText: 2022 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import android.widget.ImageView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ltd.evilcorp.core.vo.Contact
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AvatarFactoryTest {
    @Test
    fun emptyName() {
        val imageView = ImageView(InstrumentationRegistry.getInstrumentation().targetContext)
        AvatarFactory(Contact(publicKey = "123")).assignInto(imageView)
    }

    @Test
    fun nameEndingInSpace() {
        val imageView = ImageView(InstrumentationRegistry.getInstrumentation().targetContext)
        AvatarFactory(Contact(publicKey = "123", name = "a ")).assignInto(imageView)
    }
}
