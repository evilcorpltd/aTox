package ltd.evilcorp.atox.ui.call

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ltd.evilcorp.atox.databinding.FragmentCallBinding
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.atox.ui.setAvatarFromContact
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.domain.tox.PublicKey

private val PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
private const val REQUEST_RECORD_AUDIO_PERMISSION = 8888

class CallFragment : BaseFragment<FragmentCallBinding>(FragmentCallBinding::inflate) {
    private val vm: CallViewModel by viewModels { vmFactory }

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

        if (vm.inCall()) {
            return
        }

        if (ContextCompat.checkSelfPermission(requireContext(), PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED) {
            vm.startCall()
            return
        }

        requestPermissions(PERMISSIONS, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val granted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }

        if (!granted) {
            findNavController().popBackStack()
        } else {
            vm.startCall()
        }
    }
}
