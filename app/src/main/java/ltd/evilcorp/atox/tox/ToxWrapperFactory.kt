package ltd.evilcorp.atox.tox

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToxWrapperFactory @Inject constructor(private val saveManager: SaveManager) {
    fun create(options: SaveOptions, eventListener: ToxEventListener) = ToxWrapper(eventListener, saveManager, options)
}
