package ltd.evilcorp.atox

import android.os.StrictMode
import androidx.multidex.MultiDexApplication
import ltd.evilcorp.atox.di.AppComponent
import ltd.evilcorp.atox.di.DaggerAppComponent

class App : MultiDexApplication() {
    val component: AppComponent by lazy {
        componentOverride ?: DaggerAppComponent.factory().create(applicationContext)
    }

    var componentOverride: AppComponent? = null

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) StrictMode.enableDefaults()
    }
}
