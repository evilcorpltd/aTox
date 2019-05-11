package ltd.evilcorp.atox

import im.tox.tox4j.impl.jni.ToxCoreImpl
import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.core.options.ProxyOptions

class Tox {

    private var tox: ToxCoreImpl? = null

    fun init() {
        val opts = ToxOptions(
            true,
            true,
            true,
            null,
            0,
            0,
            0,
            null,
            true
        )

        tox = ToxCoreImpl(opts)
    }
}
