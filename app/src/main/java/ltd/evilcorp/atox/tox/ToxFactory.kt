package ltd.evilcorp.atox.tox

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToxFactory @Inject constructor(private val saveManager: SaveManager) {
    fun create(saveOption: SaveOptions, eventListener: ToxEventListener) = Tox(eventListener, saveManager, saveOption)
}
