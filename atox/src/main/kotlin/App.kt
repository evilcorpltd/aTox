// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import androidx.multidex.MultiDexApplication
import ltd.evilcorp.atox.di.appModule
import ltd.evilcorp.atox.di.coreModule
import ltd.evilcorp.atox.di.daoModule
import ltd.evilcorp.atox.di.databaseModule
import ltd.evilcorp.atox.di.domainModule
import ltd.evilcorp.atox.di.viewModelModule
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.diContext

class App : MultiDexApplication(), DIAware {
    var testModule: DI.Module? = null

    override val di by DI.lazy {
        importOnce(androidXModule(this@App))
        importOnce(appModule(diContext(this@App)))
        importOnce(coreModule())
        importOnce(daoModule())
        importOnce(databaseModule())
        importOnce(domainModule(diContext(this@App)))
        importOnce(viewModelModule(this@App))

        testModule?.let {
            import(it, allowOverride = true)
        }
    }
}
