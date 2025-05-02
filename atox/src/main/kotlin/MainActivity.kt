// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.findNavController
import javax.inject.Inject
import ltd.evilcorp.atox.di.ViewModelFactory
import ltd.evilcorp.atox.settings.Settings
import ltd.evilcorp.atox.ui.contactlist.ARG_SHARE
import ltd.evilcorp.domain.tox.ToxID

private const val TAG = "MainActivity"

// TODO(robinlinden): Move to common place, use for QR-code-provided uris as well.
data class ToxUri(val toxId: ToxID, val nameSuggestion: String? = null, val messageSuggestion: String? = null) {
    companion object {
        private const val TOX_ID_LENGTH = 76

        fun parseFrom(uri: Uri?): ToxUri? {
            if (uri == null) {
                return null
            }

            if (uri.scheme != "tox") {
                return null
            }

            if (uri.isOpaque) {
                if (uri.schemeSpecificPart.length != TOX_ID_LENGTH) {
                    return null
                }

                return ToxUri(ToxID(uri.schemeSpecificPart))
            }

            val maybeToxId = uri.host
            if (maybeToxId?.length != TOX_ID_LENGTH) {
                return null
            }

            return ToxUri(ToxID(maybeToxId), uri.getQueryParameter("name"), uri.getQueryParameter("message"))
        }
    }
}

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

    override fun onNewIntent(intent: Intent) {
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

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> handleToxLinkIntent(intent)
            Intent.ACTION_SEND -> handleShareIntent(intent)
        }
    }

    private fun handleToxLinkIntent(intent: Intent) {
        val data = intent.data
        Log.i(TAG, "Got uri with data: $data")
        val toxUri = ToxUri.parseFrom(data)
        if (toxUri == null) {
            Log.e(TAG, "Got malformed uri: $data")
            return
        }

        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()?.navigate(
            R.id.addContactFragment,
            bundleOf(
                "toxId" to toxUri.toxId.string(),
                "name" to toxUri.nameSuggestion,
                "message" to toxUri.messageSuggestion,
            ),
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
