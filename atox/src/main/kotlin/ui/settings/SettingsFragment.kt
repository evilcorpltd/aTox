package ltd.evilcorp.atox.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.vmFactory

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val vm: SettingsViewModel by viewModels { vmFactory }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) =
        setPreferencesFromResource(R.xml.preferences, rootKey)

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        when (key) {
            "name" -> vm.setName(
                preferences.getString(key, null) ?: getString(R.string.name_default)
            )
            "status_message" -> vm.setStatusMessage(preferences.getString(key, null) ?: "")
        }
    }
}
