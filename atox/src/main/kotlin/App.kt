package ltd.evilcorp.atox

import androidx.annotation.VisibleForTesting
import androidx.multidex.MultiDexApplication
import ltd.evilcorp.atox.di.AppComponent
import ltd.evilcorp.atox.di.DaggerAppComponent

class App : MultiDexApplication() {
    val component: AppComponent by lazy {
        componentOverride ?: DaggerAppComponent.factory().create(applicationContext)
    }

    @VisibleForTesting
    var componentOverride: AppComponent? = null
}
