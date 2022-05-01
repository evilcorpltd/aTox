// SPDX-FileCopyrightText: 2020-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import android.app.Activity
import androidx.room.Room
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import ltd.evilcorp.core.db.Database
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.SaveManager
import org.hamcrest.core.AllOf.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

class InjectedActivityTestRule<T : Activity>(
    activityClass: Class<T>,
    private val listener: () -> Unit
) : ActivityTestRule<T>(activityClass, false, true) {
    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()
        listener()
    }
}

class SaveManagerImplMock : SaveManager {
    override fun list(): List<String> = listOf("workaround")
    override fun save(publicKey: PublicKey, saveData: ByteArray) {}
    override fun load(publicKey: PublicKey): ByteArray? = null
}

fun testModule() = DI.Module("TestModule") {
    bind(overrides = true) { singleton { Room.inMemoryDatabaseBuilder(instance(), Database::class.java).build() } }
    bind<SaveManager>(overrides = true) { singleton { SaveManagerImplMock() } }
}

@RunWith(AndroidJUnit4::class)
class IntegrationTest {
    @get:Rule
    val activityRule = InjectedActivityTestRule(MainActivity::class.java) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext.applicationContext as App
        app.testModule = testModule()
    }

    @Test
    fun profileCreationWorks() {
        // ProfileFragment
        onView(withId(R.id.username)).perform(typeText("mr robotto"), closeSoftKeyboard())
        onView(withId(R.id.btnCreate)).perform(click())

        // ContactListFragment
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open())
        onView(withId(R.id.profileName)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.profileName), withText("mr robotto")))
            .check(matches(isDisplayed()))
    }
}
