package ltd.evilcorp.atox

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import ltd.evilcorp.atox.settings.Settings
import javax.inject.Inject

class ActivityLauncher : Activity() {

    @Inject
    lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).component.inject(this)
        super.onCreate(savedInstanceState)

        val newIntent = intent.clone() as Intent

        if (settings.wipUI) {
            newIntent.setClass(applicationContext, NewMainActivity::class.java)
        } else {
            newIntent.setClass(applicationContext, MainActivity::class.java)
        }
        startActivity(newIntent)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}
