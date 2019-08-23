package ltd.evilcorp.atox

import android.app.Application
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import ltd.evilcorp.atox.di.AppInjector
import javax.inject.Inject

class App : Application(), HasAndroidInjector {
    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()
        AppInjector.inject(this)
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}
