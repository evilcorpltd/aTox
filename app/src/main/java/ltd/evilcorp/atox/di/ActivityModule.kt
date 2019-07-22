package ltd.evilcorp.atox.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ltd.evilcorp.atox.MainActivity

@Suppress("unused")
@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun mainActivityInjector(): MainActivity
}
