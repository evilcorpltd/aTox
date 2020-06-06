package ltd.evilcorp.atox

import androidx.multidex.MultiDexApplication
import ltd.evilcorp.atox.di.AppComponent
import ltd.evilcorp.atox.di.DaggerAppComponent

class App : MultiDexApplication() {
    val component: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }
}
