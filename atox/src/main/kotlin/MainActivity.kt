package ltd.evilcorp.atox

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import javax.inject.Inject
import ltd.evilcorp.atox.di.ViewModelFactory
import ltd.evilcorp.atox.settings.Settings

private const val TAG = "MainActivity"
private const val SCHEME = "tox:"
private const val TOX_ID_LENGTH = 76

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var vmFactory: ViewModelFactory

    @Inject
    lateinit var autoAway: AutoAway

    @Inject
    lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).component.inject(this)

        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(settings.theme)

        setContentView(R.layout.activity_main)

        // Handle potential tox link.
        if (intent.action == Intent.ACTION_VIEW) {
            val data = intent.dataString ?: ""
            Log.i(TAG, "Got uri with data: $data")
            if (!data.startsWith(SCHEME) || data.length != SCHEME.length + TOX_ID_LENGTH) {
                Log.e(TAG, "Got malformed uri: $data")
                return
            }

            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()?.navigate(
                R.id.action_contactListFragment_to_addContactFragment,
                bundleOf("toxId" to data.drop(SCHEME.length))
            )
        }
    }

    override fun onPause() {
        super.onPause()
        autoAway.onBackground()
    }

    override fun onResume() {
        super.onResume()
        autoAway.onForeground()
    }
}
