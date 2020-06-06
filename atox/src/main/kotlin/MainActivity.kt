package ltd.evilcorp.atox

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import ltd.evilcorp.atox.di.ViewModelFactory
import javax.inject.Inject

private const val TAG = "MainActivity"
private const val SCHEME = "tox:"
private const val TOX_ID_LENGTH = 76

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var vmFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).component.inject(this)

        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(
            PreferenceManager.getDefaultSharedPreferences(applicationContext).getInt("theme", 0)
        )

        setContentView(R.layout.activity_main)

        // Handle potential tox link.
        if (intent.action == Intent.ACTION_VIEW) {
            val data = intent.dataString ?: ""
            Log.i(TAG, "Got uri with data: $data")
            if (!data.startsWith(SCHEME) || data.length != SCHEME.length + TOX_ID_LENGTH) {
                Log.e(TAG, "Got malformed uri: $data")
                return
            }

            nav_host_fragment.findNavController().navigate(
                R.id.action_contactListFragment_to_addContactFragment,
                Bundle().apply { putString("toxId", data.drop(SCHEME.length)) }
            )
        }
    }
}
