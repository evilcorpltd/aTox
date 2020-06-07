package ltd.evilcorp.domain.tox

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class ToxTest {
    @Test
    fun quitting_does_not_crash() {
        for (i in 1..10) {
            val tox = Tox(mockk(relaxUnitFun = true), mockk(relaxUnitFun = true))
            tox.start(SaveOptions(null, false), ToxEventListener(), ToxAvEventListener())
            sleep(25)
            tox.stop()
        }
    }
}
