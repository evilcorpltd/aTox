package ltd.evilcorp.domain.tox

import io.mockk.mockk
import org.junit.Test
import java.lang.Thread.sleep

class ToxTest {
    @Test
    fun quitting_does_not_crash() {
        for (i in 1..10) {
            val tox = Tox(mockk(relaxUnitFun = true), mockk(relaxUnitFun = true))
            tox.start(SaveOptions(null, false), ToxEventListener())
            sleep(25)
            tox.stop()
        }
    }
}
