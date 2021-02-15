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
