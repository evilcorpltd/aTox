// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.di

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ltd.evilcorp.atox.tox.BootstrapNodeRegistryImpl
import ltd.evilcorp.domain.tox.BootstrapNodeRegistry

@Module
class AppModule {
    @Provides
    fun provideBootstrapNodeRegistry(nodeRegistry: BootstrapNodeRegistryImpl): BootstrapNodeRegistry = nodeRegistry

    @Provides
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.Default)
}
