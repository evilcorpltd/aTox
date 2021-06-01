package ltd.evilcorp.atox.ui.call

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import ltd.evilcorp.atox.databinding.FragmentCallBinding
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.atox.ui.setAvatarFromContact
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.domain.feature.CallState
import ltd.evilcorp.domain.tox.PublicKey

private const val PERMISSION = Manifest.permission.RECORD_AUDIO

class CallFragment : BaseFragment<FragmentCallBinding>(FragmentCallBinding::inflate) {
    private val vm: CallViewModel by viewModels { vmFactory }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCall()
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars())
            controlContainer.updatePadding(bottom = insets.bottom)
            compat
        }

        vm.setActiveContact(PublicKey(requireStringArg(CONTACT_PUBLIC_KEY)))
        vm.contact.observe(viewLifecycleOwner) {
            setAvatarFromContact(callBackground, it)
        }

        endCall.setOnClickListener {
            vm.endCall()
            findNavController().popBackStack()
        }

        if (vm.inCall.value is CallState.InCall) {
            vm.inCall.asLiveData().observe(viewLifecycleOwner) { inCall ->
                if (inCall == CallState.NotInCall) {
                    findNavController().popBackStack()
                }
            }
            return
        }

        if (ContextCompat.checkSelfPermission(requireContext(), PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            startCall()
            return
        }

        requestPermissionLauncher.launch(PERMISSION)
    }

    private fun startCall() {
        vm.startCall()
        vm.inCall.asLiveData().observe(viewLifecycleOwner) { inCall ->
            if (inCall == CallState.NotInCall) {
                findNavController().popBackStack()
            }
        }
    }
}
