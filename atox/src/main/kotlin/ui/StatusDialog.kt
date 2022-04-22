// SPDX-FileCopyrightText: 2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.Window
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.DialogStatusBinding
import ltd.evilcorp.core.vo.UserStatus

private const val TRANSITION_TIME = 250

class StatusDialog(
    ctx: Context,
    private var activeStatus: UserStatus,
    private val setStatusFunc: (UserStatus) -> Unit
) : Dialog(ctx, R.style.DialogSlideAnimation) {
    private var _binding: DialogStatusBinding? = null
    private val binding get() = _binding!!

    private fun viewByStatus(status: UserStatus): TransitionDrawable = when (status) {
        UserStatus.None -> binding.statusAvailable.background as TransitionDrawable
        UserStatus.Away -> binding.statusAway.background as TransitionDrawable
        UserStatus.Busy -> binding.statusBusy.background as TransitionDrawable
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        _binding = DialogStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        selectStatus(activeStatus)
        binding.run {
            statusAvailable.setOnClickListener { selectStatus(UserStatus.None) }
            statusAway.setOnClickListener { selectStatus(UserStatus.Away) }
            statusBusy.setOnClickListener { selectStatus(UserStatus.Busy) }

            cancel.setOnClickListener { dismiss() }
            confirm.setOnClickListener {
                setStatusFunc(activeStatus)
                dismiss()
            }
        }
    }

    private fun selectStatus(status: UserStatus) {
        viewByStatus(activeStatus).reverseTransition(TRANSITION_TIME)
        activeStatus = status
        viewByStatus(activeStatus).startTransition(TRANSITION_TIME)
    }
}
