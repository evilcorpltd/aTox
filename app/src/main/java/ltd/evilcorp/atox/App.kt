package ltd.evilcorp.atox

import android.app.Activity
import android.app.Application
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import ltd.evilcorp.atox.di.AppInjector
import ltd.evilcorp.atox.tox.ToxThread
import javax.inject.Inject

class App : Application(), HasActivityInjector {
    companion object {
        lateinit var profile: String
        lateinit var password: String
        lateinit var toxThread: ToxThread
    }

    @Inject
    lateinit var injector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        AppInjector.inject(this)
    }

    override fun activityInjector(): DispatchingAndroidInjector<Activity> = injector
}
