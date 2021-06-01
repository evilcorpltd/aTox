package ltd.evilcorp.atox.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import java.lang.NumberFormatException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ltd.evilcorp.atox.BuildConfig
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentSettingsBinding
import ltd.evilcorp.atox.settings.BootstrapNodeSource
import ltd.evilcorp.atox.settings.FtAutoAccept
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.domain.tox.ProxyType

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {
    private val vm: SettingsViewModel by viewModels { vmFactory }
    private val blockBackCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            Toast.makeText(requireContext(), getString(R.string.warn_proxy_broken), Toast.LENGTH_LONG).show()
        }
    }

    private val applySettingsCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            vm.commit()
        }
    }

    private val importNodesLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        GlobalScope.launch {
            if (uri != null && vm.validateNodeJson(uri)) {
                if (vm.importNodeJson(uri)) {
                    vm.setBootstrapNodeSource(BootstrapNodeSource.UserProvided)
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    binding.settingBootstrapNodes.setSelection(BootstrapNodeSource.BuiltIn.ordinal)

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.warn_node_json_import_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, applySettingsCallback)
        requireActivity().onBackPressedDispatcher.addCallback(this, blockBackCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            toolbar.updatePadding(top = insets.top)
            v.updatePadding(left = insets.left, right = insets.right)
            version.updatePadding(bottom = insets.bottom)
            compat
        }

        toolbar.apply {
            setNavigationIcon(R.drawable.back)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
        }

        theme.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.pref_theme_options,
            android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        theme.setSelection(vm.getTheme())

        theme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                vm.setTheme(position)
            }
        }

        settingRunAtStartup.isChecked = vm.getRunAtStartup()
        settingRunAtStartup.setOnClickListener { vm.setRunAtStartup(settingRunAtStartup.isChecked) }

        settingAutoAwayEnabled.isChecked = vm.getAutoAwayEnabled()
        settingAutoAwayEnabled.setOnClickListener { vm.setAutoAwayEnabled(settingAutoAwayEnabled.isChecked) }

        settingAutoAwaySeconds.setText(vm.getAutoAwaySeconds().toString())
        settingAutoAwaySeconds.doAfterTextChanged {
            val str = it?.toString() ?: ""
            val seconds = try {
                str.toLong()
            } catch (e: NumberFormatException) {
                settingAutoAwaySeconds.error = getString(R.string.bad_positive_number)
                return@doAfterTextChanged
            }

            vm.setAutoAwaySeconds(seconds)
        }

        settingFtAutoAccept.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.pref_ft_auto_accept_options,
            android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        settingFtAutoAccept.setSelection(vm.getFtAutoAccept().ordinal)

        settingFtAutoAccept.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                vm.setFtAutoAccept(FtAutoAccept.values()[position])
            }
        }

        if (vm.getProxyType() != ProxyType.None) {
            vm.setUdpEnabled(false)
        }

        settingsUdpEnabled.isChecked = vm.getUdpEnabled()
        settingsUdpEnabled.isEnabled = vm.getProxyType() == ProxyType.None
        settingsUdpEnabled.setOnClickListener { vm.setUdpEnabled(settingsUdpEnabled.isChecked) }

        proxyType.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.pref_proxy_type_options, android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        proxyType.setSelection(vm.getProxyType().ordinal)

        proxyType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = ProxyType.values()[position]
                vm.setProxyType(selected)

                // Disable UDP if a proxy is selected to ensure all traffic goes through the proxy.
                settingsUdpEnabled.isEnabled = selected == ProxyType.None
                settingsUdpEnabled.isChecked = settingsUdpEnabled.isChecked && selected == ProxyType.None
                vm.setUdpEnabled(settingsUdpEnabled.isChecked)
            }
        }

        proxyAddress.setText(vm.getProxyAddress())
        proxyAddress.doAfterTextChanged { vm.setProxyAddress(it?.toString() ?: "") }

        proxyPort.setText(vm.getProxyPort().toString())
        proxyPort.doAfterTextChanged {
            val str = it?.toString() ?: ""
            val port = try {
                Integer.parseInt(str)
            } catch (e: NumberFormatException) {
                proxyPort.error = getString(R.string.bad_port)
                return@doAfterTextChanged
            }

            if (port < 1 || port > 65535) {
                proxyPort.error = getString(R.string.bad_port)
                return@doAfterTextChanged
            }

            vm.setProxyPort(port)
        }

        vm.proxyStatus.observe(viewLifecycleOwner) { status: ProxyStatus ->
            proxyStatus.text = when (status) {
                ProxyStatus.Good -> ""
                ProxyStatus.BadPort -> getString(R.string.bad_port)
                ProxyStatus.BadHost -> getString(R.string.bad_host)
                ProxyStatus.BadType -> getString(R.string.bad_type)
                ProxyStatus.NotFound -> getString(R.string.proxy_not_found)
            }
            blockBackCallback.isEnabled = proxyStatus.text.isNotEmpty()
        }
        vm.checkProxy()

        vm.committed.observe(viewLifecycleOwner) { committed ->
            if (committed) {
                findNavController().popBackStack()
            }
        }

        nospam.setText("%08X".format(vm.getNospam()))
        nospam.doAfterTextChanged {
            saveNospam.isEnabled =
                nospam.text.length == 8 && nospam.text.toString().toUInt(16).toInt() != vm.getNospam()
        }
        saveNospam.isEnabled = false
        saveNospam.setOnClickListener {
            vm.setNospam(nospam.text.toString().toUInt(16).toInt())
            saveNospam.isEnabled = false
            Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_LONG).show()
        }

        settingBootstrapNodes.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.pref_bootstrap_node_options,
            android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        settingBootstrapNodes.setSelection(vm.getBootstrapNodeSource().ordinal)

        settingBootstrapNodes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val source = BootstrapNodeSource.values()[position]

                // Hack to avoid triggering the document chooser again if the user has set it to UserProvided.
                if (source == vm.getBootstrapNodeSource()) return

                if (source == BootstrapNodeSource.BuiltIn) {
                    vm.setBootstrapNodeSource(source)
                } else {
                    importNodesLauncher.launch(arrayOf("application/json"))
                }
            }
        }

        version.text = getString(R.string.version_display, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }
}
