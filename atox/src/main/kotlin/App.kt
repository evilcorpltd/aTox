package ltd.evilcorp.atox

import androidx.multidex.MultiDexApplication
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import ltd.evilcorp.atox.di.AppInjector
import javax.inject.Inject

class App : MultiDexApplication(), HasAndroidInjector {
    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()
        AppInjector.inject(this)
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}
