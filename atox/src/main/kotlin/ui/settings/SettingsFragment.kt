package ltd.evilcorp.atox.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import ltd.evilcorp.atox.BuildConfig
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentSettingsBinding
import ltd.evilcorp.atox.setUpFullScreenUi
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.vmFactory

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {
    private val vm: SettingsViewModel by viewModels { vmFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        view.setUpFullScreenUi { v, insets ->
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

        settingRunAtStartup.isChecked = vm.getRunAtStartup()
        settingRunAtStartup.setOnClickListener {
            vm.setRunAtStartup(settingRunAtStartup.isChecked)
        }

        settingsUdpEnabled.isChecked = vm.getUdpEnabled()
        settingsUdpEnabled.setOnClickListener {
            vm.setUdpEnabled(settingsUdpEnabled.isChecked)
        }

        version.text = getString(R.string.version_display, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    override fun onStop() {
        super.onStop()
        vm.commit()
    }
}
