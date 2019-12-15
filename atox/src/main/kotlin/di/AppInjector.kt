package ltd.evilcorp.atox.di

import ltd.evilcorp.atox.App

object AppInjector {
    fun inject(app: App) {
        DaggerAppComponent.builder()
            .applicationModule(ApplicationModule(app))
            .build()
            .inject(app)
    }
}
