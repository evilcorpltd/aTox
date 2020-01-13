package ltd.evilcorp.atox.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.settings_fragment.*
import kotlinx.android.synthetic.main.settings_fragment.view.*
import ltd.evilcorp.atox.BuildConfig
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.vmFactory

class SettingsFragment : Fragment() {
    private val vm: SettingsViewModel by viewModels { vmFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.settings_fragment, container, false).apply {
        toolbar.apply {
            setNavigationIcon(R.drawable.back)
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }

        vm.user.observe(viewLifecycleOwner, Observer { user ->
            name.setText(user.name)
            statusMessage.setText(user.statusMessage)
        })

        theme.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.pref_theme_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        theme.setSelection(
            PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt("theme", 0)
        )

        theme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                PreferenceManager.getDefaultSharedPreferences(requireContext()).edit {
                    putInt("theme", position)
                }

                AppCompatDelegate.setDefaultNightMode(position)
            }
        }

        version.text =
            getString(R.string.version_display, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    override fun onPause() {
        super.onPause()
        vm.setName(name.text.toString())
        vm.setStatusMessage(statusMessage.text.toString())
    }
}
