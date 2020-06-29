package ltd.evilcorp.atox.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton
import ltd.evilcorp.atox.BootReceiver
import ltd.evilcorp.atox.MainActivity
import ltd.evilcorp.atox.ToxService
import ltd.evilcorp.core.di.DaoModule
import ltd.evilcorp.core.di.DatabaseModule

@Singleton
@Component(modules = [AndroidModule::class, DatabaseModule::class, DaoModule::class, ViewModelModule::class])
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance appContext: Context): AppComponent
    }

    fun inject(activity: MainActivity)
    fun inject(service: ToxService)
    fun inject(receiver: BootReceiver)
}
