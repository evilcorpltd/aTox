// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.call

import android.Manifest
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentCallBinding
import ltd.evilcorp.atox.hasPermission
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.atox.ui.setAvatarFromContact
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.domain.feature.CallState
import ltd.evilcorp.domain.tox.PublicKey

private const val PERMISSION = Manifest.permission.RECORD_AUDIO

private fun dpToPx(dp: Float, res: Resources): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.displayMetrics).toInt()

class CallFragment : BaseFragment<FragmentCallBinding>(FragmentCallBinding::inflate) {
    private val vm: CallViewModel by viewModels { vmFactory }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            vm.startSendingAudio()
        } else {
            Toast.makeText(requireContext(), getString(R.string.call_mic_permission_needed), Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars())
            controlContainer.updatePadding(bottom = insets.bottom + dpToPx(16f, resources))
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

        vm.sendingAudio.asLiveData().observe(viewLifecycleOwner) { sending ->
            if (sending) {
                microphoneControl.setImageResource(R.drawable.ic_mic)
            } else {
                microphoneControl.setImageResource(R.drawable.ic_mic_off)
            }
        }

        microphoneControl.setOnClickListener {
            if (vm.sendingAudio.value) {
                vm.stopSendingAudio()
            } else {
                if (requireContext().hasPermission(PERMISSION)) {
                    vm.startSendingAudio()
                } else {
                    requestPermissionLauncher.launch(PERMISSION)
                }
            }
        }

        if (vm.inCall.value is CallState.InCall) {
            vm.inCall.asLiveData().observe(viewLifecycleOwner) { inCall ->
                if (inCall == CallState.NotInCall) {
                    findNavController().popBackStack()
                }
            }
            return
        }

        startCall()

        if (requireContext().hasPermission(PERMISSION)) {
            vm.startSendingAudio()
        }
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
