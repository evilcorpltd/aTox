// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.di

import dagger.Module
import dagger.Provides
import ltd.evilcorp.atox.tox.BootstrapNodeRegistryImpl
import ltd.evilcorp.domain.tox.BootstrapNodeRegistry

@Module
class AppModule {
    @Provides
    fun provideBootstrapNodeRegistry(nodeRegistry: BootstrapNodeRegistryImpl): BootstrapNodeRegistry = nodeRegistry
}
