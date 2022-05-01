// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.findNavController
import ltd.evilcorp.atox.settings.Settings
import ltd.evilcorp.atox.ui.contactlist.ARG_SHARE
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

private const val TAG = "MainActivity"
private const val SCHEME = "tox:"
private const val TOX_ID_LENGTH = 76

class MainActivity : AppCompatActivity(), DIAware {
    override val di by closestDI()

    private val autoAway: AutoAway by instance()
    private val settings: Settings by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (settings.disableScreenshots) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        AppCompatDelegate.setDefaultNightMode(settings.theme)

        // The view inset/padding adjustments only run for Lollipop and newer.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        setContentView(R.layout.activity_main)

        // Only handle intent the first time it triggers the app.
        if (savedInstanceState != null) return
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onPause() {
        super.onPause()
        autoAway.onBackground()
    }

    override fun onResume() {
        super.onResume()
        autoAway.onForeground()
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_VIEW -> handleToxLinkIntent(intent)
            Intent.ACTION_SEND -> handleShareIntent(intent)
        }
    }

    private fun handleToxLinkIntent(intent: Intent) {
        val data = intent.dataString ?: ""
        Log.i(TAG, "Got uri with data: $data")
        if (!data.startsWith(SCHEME) || data.length != SCHEME.length + TOX_ID_LENGTH) {
            Log.e(TAG, "Got malformed uri: $data")
            return
        }

        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()?.navigate(
            R.id.addContactFragment,
            bundleOf("toxId" to data.drop(SCHEME.length))
        )
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
        navController.navigate(R.id.contactListFragment, bundleOf(ARG_SHARE to data))
    }
}
