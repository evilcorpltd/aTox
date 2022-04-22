// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.call

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentCallBinding
import ltd.evilcorp.atox.hasPermission
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.domain.feature.CallState
import ltd.evilcorp.domain.tox.PublicKey
import org.kodein.di.android.x.viewmodel.viewModel

private const val PERMISSION = Manifest.permission.RECORD_AUDIO

class CallFragment : BaseFragment<FragmentCallBinding>(FragmentCallBinding::inflate) {
    private val vm: CallViewModel by viewModel()

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
            controlContainer.updatePadding(bottom = insets.bottom + controlContainer.paddingTop)
            compat
        }

        vm.setActiveContact(PublicKey(requireStringArg(CONTACT_PUBLIC_KEY)))
        vm.contact.observe(viewLifecycleOwner) {
            avatarImageView.setFrom(it)
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

        updateSpeakerphoneIcon()
        speakerphone.setOnClickListener {
            vm.speakerphoneOn = !vm.speakerphoneOn
            updateSpeakerphoneIcon()
        }

        backToChat.setOnClickListener {
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

        startCall()

        if (requireContext().hasPermission(PERMISSION)) {
            vm.startSendingAudio()
        }
    }

    private fun updateSpeakerphoneIcon() {
        val icon = if (vm.speakerphoneOn) R.drawable.ic_speakerphone else R.drawable.ic_speakerphone_off
        binding.speakerphone.setImageResource(icon)
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
