package ltd.evilcorp.atox

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ltd.evilcorp.atox.activity.ContactListActivity
import ltd.evilcorp.atox.activity.ProfileActivity

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun profileInjector(): ProfileActivity

    @ContributesAndroidInjector
    abstract fun contactListInjector(): ContactListActivity
}
