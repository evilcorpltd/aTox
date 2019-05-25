package ltd.evilcorp.atox

import android.app.Activity
import android.app.Application
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject
import javax.inject.Singleton

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
        DaggerAppComponent.builder()
            .contactModule(ContactModule(this))
            .build()
            .inject(this)
    }

    override fun activityInjector(): DispatchingAndroidInjector<Activity> = injector
}

@Singleton
@Component(modules = [AndroidInjectionModule::class, ActivityModule::class, ContactModule::class])
interface AppComponent : AndroidInjector<App>
