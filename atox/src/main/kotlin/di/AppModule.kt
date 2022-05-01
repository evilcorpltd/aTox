// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ltd.evilcorp.atox.AutoAway
import ltd.evilcorp.atox.settings.Settings
import ltd.evilcorp.atox.tox.BootstrapNodeRegistryImpl
import ltd.evilcorp.atox.tox.EventListenerCallbacks
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.atox.ui.NotificationHelper
import ltd.evilcorp.domain.tox.BootstrapNodeRegistry
import org.kodein.di.DI
import org.kodein.di.DIContext
import org.kodein.di.bind
import org.kodein.di.provider
import org.kodein.di.singleton

fun appModule(appContext: DIContext<*>) = DI.Module(name = "AppModule") {
    bind<BootstrapNodeRegistry> { singleton { BootstrapNodeRegistryImpl(di) } }
    bind { singleton { AutoAway(di) } }
    bind { singleton { EventListenerCallbacks(di, appContext) } }
    bind { singleton { NotificationHelper(di) } }
    bind { singleton { Settings(di, appContext) } }
    bind { singleton { ToxStarter(di) } }
    bind { provider { CoroutineScope(Dispatchers.Default) } }
}
