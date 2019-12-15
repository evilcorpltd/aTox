package ltd.evilcorp.atox.di

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import ltd.evilcorp.atox.App
import ltd.evilcorp.core.di.DatabaseModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AndroidModule::class,
        ApplicationModule::class,
        DatabaseModule::class,
        ViewModelModule::class
    ]
)
interface AppComponent : AndroidInjector<App>
