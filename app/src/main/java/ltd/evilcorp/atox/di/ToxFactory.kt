package ltd.evilcorp.atox.di

import android.content.Context
import im.tox.tox4j.core.options.ProxyOptions
import im.tox.tox4j.core.options.SaveDataOptions
import im.tox.tox4j.core.options.ToxOptions
import ltd.evilcorp.atox.tox.Tox
import ltd.evilcorp.atox.tox.ToxEventListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToxFactory @Inject constructor(
    private val context: Context,
    private val eventListener: ToxEventListener
) {
    fun create(saveOption: SaveDataOptions) = Tox(
        context,
        eventListener,
        ToxOptions(
            true,
            true,
            true,
            ProxyOptions.`None$`(),
            0,
            0,
            0,
            saveOption,
            true
        )
    )
}
