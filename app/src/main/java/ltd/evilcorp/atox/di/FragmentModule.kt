package ltd.evilcorp.atox.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ltd.evilcorp.atox.ui.chat.ChatFragment

@Suppress("unused")
@Module
abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract fun chatInjector(): ChatFragment
}
