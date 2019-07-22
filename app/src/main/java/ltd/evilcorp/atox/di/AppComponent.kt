package ltd.evilcorp.atox.di

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import ltd.evilcorp.atox.App
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ActivityModule::class,
        AndroidInjectionModule::class,
        ApplicationModule::class,
        DatabaseModule::class,
        ViewModelModule::class
    ]
)
interface AppComponent : AndroidInjector<App>
