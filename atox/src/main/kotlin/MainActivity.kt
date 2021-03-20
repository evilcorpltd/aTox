package ltd.evilcorp.atox

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import javax.inject.Inject
import ltd.evilcorp.atox.di.ViewModelFactory
import ltd.evilcorp.atox.settings.Settings
import ltd.evilcorp.atox.ui.contactlist.ARG_SHARE

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

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        // Only handle intent the first time it triggers the app.
        if (savedInstanceState != null) return
        when (intent.action) {
            // Handle potential tox link
            Intent.ACTION_VIEW -> {
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
            Intent.ACTION_SEND -> handleShareIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.action != Intent.ACTION_SEND) return
        val nav = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
        if (nav == null) {
            Log.e(TAG, "Unable to find navController to handle intent $intent")
            return
        }

        nav.popBackStack(R.id.contactListFragment, false)
        handleShareIntent(intent)
    }

    override fun onPause() {
        super.onPause()
        autoAway.onBackground()
    }

    override fun onResume() {
        super.onResume()
        autoAway.onForeground()
    }

    private fun handleShareIntent(intent: Intent) {
        if (intent.type != "text/plain") {
            Log.e(TAG, "Got unsupported share type ${intent.type}")
            return
        }

        val data = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (data.isNullOrEmpty()) {
            Log.e(TAG, "Got share intent with no data")
            return
        }

        Log.i(TAG, "Got text share: $data")
        val navController =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController() ?: return
        navController.setGraph(navController.graph, bundleOf(ARG_SHARE to data))
    }
}
