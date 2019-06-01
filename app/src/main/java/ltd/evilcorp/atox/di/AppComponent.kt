package ltd.evilcorp.atox.di

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import ltd.evilcorp.atox.App
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, ApplicationModule::class, ActivityModule::class, DatabaseModule::class])
interface AppComponent : AndroidInjector<App>
