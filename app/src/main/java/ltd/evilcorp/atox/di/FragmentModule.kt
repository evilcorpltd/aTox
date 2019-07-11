package ltd.evilcorp.atox.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ltd.evilcorp.atox.ui.chat.ChatFragment
import ltd.evilcorp.atox.ui.contactlist.ContactListFragment
import ltd.evilcorp.atox.ui.profile.ProfileFragment

@Suppress("unused")
@Module
abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract fun chatInjector(): ChatFragment

    @ContributesAndroidInjector
    abstract fun contactListInjector(): ContactListFragment

    @ContributesAndroidInjector
    abstract fun profileInjector(): ProfileFragment
}
