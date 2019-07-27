package ltd.evilcorp.atox.di

import ltd.evilcorp.atox.tox.SaveManager
import ltd.evilcorp.atox.tox.SaveOptions
import ltd.evilcorp.atox.tox.Tox
import ltd.evilcorp.atox.tox.ToxEventListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToxFactory @Inject constructor(
    private val eventListener: ToxEventListener,
    private val saveManager: SaveManager
) {
    fun create(saveOption: SaveOptions) = Tox(eventListener, saveManager, saveOption)
}
