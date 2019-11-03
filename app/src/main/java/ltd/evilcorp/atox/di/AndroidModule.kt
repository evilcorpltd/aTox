package ltd.evilcorp.atox.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ltd.evilcorp.atox.MainActivity
import ltd.evilcorp.atox.ToxService

@Suppress("unused")
@Module
abstract class AndroidModule {
    @ContributesAndroidInjector
    abstract fun mainActivityInjector(): MainActivity

    @ContributesAndroidInjector
    abstract fun toxServiceInjector(): ToxService
}
