package ltd.evilcorp.atox.tox

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToxFactory @Inject constructor(
    private val eventListener: ToxEventListener,
    private val saveManager: SaveManager
) {
    fun create(saveOption: SaveOptions) = Tox(eventListener, saveManager, saveOption)
}
