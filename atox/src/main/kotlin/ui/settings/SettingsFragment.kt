package ltd.evilcorp.atox.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.fragment_settings.view.*
import ltd.evilcorp.atox.BuildConfig
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.setUpFullScreenUi
import ltd.evilcorp.atox.vmFactory

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private val vm: SettingsViewModel by viewModels { vmFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = view.run {
        setUpFullScreenUi { v, insets ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@setUpFullScreenUi insets
            toolbar.updatePadding(top = insets.systemWindowInsetTop)
            v.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )
            version.updatePadding(bottom = insets.systemWindowInsetBottom)
            insets
        }

        toolbar.apply {
            setNavigationIcon(R.drawable.back)
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }

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

        setting_run_at_startup.isChecked = vm.getRunAtStartup()
        setting_run_at_startup.setOnClickListener {
            vm.setRunAtStartup(setting_run_at_startup.isChecked)
        }

        settings_udp_enabled.isChecked = vm.getUdpEnabled()
        settings_udp_enabled.setOnClickListener {
            vm.setUdpEnabled(settings_udp_enabled.isChecked)
        }

        version.text = getString(R.string.version_display, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    override fun onStop() {
        super.onStop()
        vm.commit()
    }
}
