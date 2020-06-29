package ltd.evilcorp.atox

import android.app.Activity
import android.content.Context
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
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import io.mockk.every
import io.mockk.mockk
import javax.inject.Singleton
import ltd.evilcorp.atox.di.AndroidModule
import ltd.evilcorp.atox.di.AppComponent
import ltd.evilcorp.atox.di.ViewModelModule
import ltd.evilcorp.core.db.Database
import ltd.evilcorp.core.di.DaoModule
import ltd.evilcorp.domain.tox.SaveManager
import org.hamcrest.core.AllOf.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

class InjectedActivityTestRule<T : Activity>(
    activityClass: Class<T>,
    private val listener: () -> Unit
) : ActivityTestRule<T>(activityClass, false, true) {
    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()
        listener()
    }
}

@Module
class TestModule {
    @Singleton
    @Provides
    fun provideDatabase(appContext: Context): Database =
        Room.inMemoryDatabaseBuilder(appContext, Database::class.java).build()

    @Provides
    fun provideSaveManager(): SaveManager = mockk(relaxUnitFun = true) {
        every { list() } returns listOf()
    }
}

@Singleton
@Component(modules = [AndroidModule::class, TestModule::class, DaoModule::class, ViewModelModule::class])
interface TestComponent : AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance appContext: Context): AppComponent
    }
}

@RunWith(AndroidJUnit4::class)
class IntegrationTest {
    @get:Rule
    val activityRule = InjectedActivityTestRule(MainActivity::class.java) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext.applicationContext as App
        app.componentOverride = DaggerTestComponent.factory().create(app)
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
