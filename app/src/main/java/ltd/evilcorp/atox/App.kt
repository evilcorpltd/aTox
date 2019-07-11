package ltd.evilcorp.atox

import android.app.Application
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import ltd.evilcorp.atox.di.AppInjector
import ltd.evilcorp.atox.tox.ToxThread
import javax.inject.Inject

class App : Application(), HasSupportFragmentInjector {
    companion object {
        var profile = "aTox user"
        lateinit var password: String
        lateinit var toxThread: ToxThread
    }

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate() {
        super.onCreate()
        AppInjector.inject(this)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector
}
