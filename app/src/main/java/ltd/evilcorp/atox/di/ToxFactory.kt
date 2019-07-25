package ltd.evilcorp.atox.di

import android.content.Context
import ltd.evilcorp.atox.tox.SaveOptions
import ltd.evilcorp.atox.tox.Tox
import ltd.evilcorp.atox.tox.ToxEventListener
import ltd.evilcorp.atox.tox.SaveManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToxFactory @Inject constructor(
    private val context: Context,
    private val eventListener: ToxEventListener,
    private val saveManager: SaveManager
) {
    fun create(saveOption: SaveOptions) = Tox(context, eventListener, saveManager, saveOption)
}
