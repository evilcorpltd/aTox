package ltd.evilcorp.atox

import android.app.Application

class App : Application() {
    companion object {
        lateinit var profile: String
        lateinit var password: String
    }
}
