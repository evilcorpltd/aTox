// SPDX-FileCopyrightText: 2021-2025 Robin Lindén <dev@robinlinden.eu>
// SPDX-FileCopyrightText: 2021-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.call

import android.Manifest
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentCallBinding
import ltd.evilcorp.atox.hasPermission
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.domain.feature.Call
import ltd.evilcorp.domain.feature.inCall
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


private const val PERMISSION = Manifest.permission.RECORD_AUDIO
private const val TAG = "CallFragment"

class CallFragment : BaseFragment<FragmentCallBinding>(FragmentCallBinding::inflate) {
    private val vm: CallViewModel by viewModels { vmFactory }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            vm.setMicrophoneOn()
            updateMicrophoneControlIcon()
        } else {
            Log.d(TAG, "Got no permission")
            Toast.makeText(requireContext(), getString(R.string.call_mic_permission_needed), Toast.LENGTH_LONG).show()
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var timerNHandle: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = binding.run {
        Log.d(TAG, "onViewCreated here")

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars())
            controlContainer.updatePadding(bottom = insets.bottom + controlContainer.paddingTop)
            compat
        }

        vm.setActiveContact(PublicKey(requireStringArg(CONTACT_PUBLIC_KEY)))
        vm.contact.observe(viewLifecycleOwner) {
            avatarImageView.setFrom(it)
            tvData.setText(it.name)
        }

        endCall.setOnClickListener {
            Log.d(TAG, "finishing by End Call")
            vm.endCall()
            adoptState(Call.State.IDLE)
        }

        vm.micOn = requireContext().hasPermission(PERMISSION)
        updateMicrophoneControlIcon()

        microphoneControl.setOnClickListener {
            if (! requireContext().hasPermission(PERMISSION)) {
                vm.micOn = false
                /*Toast.makeText(
                    context,
                    R.string.call_mic_permission_needed,
                    Toast.LENGTH_LONG
                ).show()*/
                requestPermissionLauncher.launch(PERMISSION)
            } else {
                vm.toggleMicrophoneControl()
            }
            updateMicrophoneControlIcon()
        }

       updateSpeakerphoneIcon()
       speakerphone.setOnClickListener {
           vm.toggleSpeakerphone()
           updateSpeakerphoneIcon()
       }

       backToChat.setOnClickListener {
           findNavController().popBackStack()
       }

       vm.callLiveData.observe(viewLifecycleOwner) { call ->
           Log.d(TAG, "observer here")
           adoptState()
       }

       if (vm.call.value.state != Call.State.IDLE
               && vm.call.value.state != Call.State.PENDING) {
           adoptState()
           return@run
       }
        binding.tvState.setText("startinng a call...") // normally, not to be seen
        vm.startCall()
    }// end onViewCreated

    /*override fun onResume() = binding.run {
        val nme = vm.call.value.state
        Log.d(TAG, "onResume here, state=$nme")
        super.onResume()
    }*/

    private fun updateSpeakerphoneIcon() {
        val icon = if (vm.speakerphoneOn) R.drawable.ic_speakerphone
                   else R.drawable.ic_speakerphone_off
        binding.speakerphone.setImageResource(icon)
    }

    private fun updateMicrophoneControlIcon() {
        val icon = if (vm.micOn) R.drawable.ic_mic
                   else R.drawable.ic_mic_off
        binding.microphoneControl.setImageResource(icon)
    }
    private fun adoptState() {
        adoptState(vm.call.value.state)
    }
    private fun adoptState(state: Call.State) {
        // may be called repeatedly, so must be idempotent
        Log.d(TAG, "adoptState, state = ${state}")
        when (state) {
            Call.State.CALLING_OUT ->  {
                binding.tvState.setText(getString(R.string.ringing))
                playConnecting()
            }
            Call.State.ANSWERED -> {
                stopPlay()
                binding.tvState.setText("talking")
                startTimer()
                if (! vm.sendingAudio.value && vm.micOn) {
                    if (requireContext().hasPermission(PERMISSION)) {
                        vm.startSendingAudio()
                    }
                }
            }
            // as LiveData never emits its init value, IDLE means the call is finished
            Call.State.IDLE -> {
                binding.tvState.setText("00000")
                stopPlay()
                findNavController().popBackStack()
            }
            else -> Log.e(TAG, "STATE = ${state}")
        }
    }

    private fun playConnecting() {
        val audioAttrContext =
            if (Build.VERSION.SDK_INT >= 30) context?.createAttributionContext("audioPlayback")
            else context
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(audioAttrContext, R.raw.connecting_ringtone)
            mediaPlayer?.setLooping(true)
        }
        mediaPlayer?.start()
    }

    private fun stopPlay() {
        mediaPlayer?.stop()
        mediaPlayer = null
    }

    private fun startTimer() {
        if (! vm.call.value.inCall()) return
        if (timerNHandle?.isActive == true) return
        val from: Long = vm.call.value.data?.startTime ?: 0
        timerNHandle = lifecycleScope.launch(Dispatchers.IO) {
            while (vm.call.value.inCall()) {
                lifecycleScope.launch {
                    val elapsed : Duration =  (SystemClock.elapsedRealtime() - from).milliseconds
                    val s = elapsed.toComponents { hours, minutes, seconds, nanoseconds ->
                        //String.format("%01d:%02d:%02d", hours, minutes, seconds)
                        vm.presentTime(hours, minutes, seconds, nanoseconds)
                    }
                    binding.tvState.setText(s)
                }
                delay(1000L)
            }
        }

    }
}
