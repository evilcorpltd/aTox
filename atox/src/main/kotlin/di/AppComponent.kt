// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ltd.evilcorp.atox.ActionReceiver
import ltd.evilcorp.atox.ActivityLauncher
import ltd.evilcorp.atox.BootReceiver
import ltd.evilcorp.atox.MainActivity
import ltd.evilcorp.atox.NewMainActivity
import ltd.evilcorp.atox.ToxService
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidModule::class,
        AppModule::class,
        DatabaseModule::class,
        DaoModule::class,
        ViewModelModule::class,
    ],
)
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance appContext: Context): AppComponent
    }

    fun inject(activity: ActivityLauncher)
    fun inject(activity: MainActivity)
    fun inject(activity: NewMainActivity)
    fun inject(service: ToxService)
    fun inject(receiver: BootReceiver)
    fun inject(receiver: ActionReceiver)
}
