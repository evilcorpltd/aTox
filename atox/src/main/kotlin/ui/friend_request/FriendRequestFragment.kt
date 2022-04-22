// SPDX-FileCopyrightText: 2020-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.friend_request

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentFriendRequestBinding
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.domain.tox.PublicKey
import org.kodein.di.android.x.viewmodel.viewModel

const val FRIEND_REQUEST_PUBLIC_KEY = "FRIEND_REQUEST_PUBLIC_KEY"

class FriendRequestFragment : BaseFragment<FragmentFriendRequestBinding>(FragmentFriendRequestBinding::inflate) {
    private val vm: FriendRequestViewModel by viewModel()
    private lateinit var friendRequest: FriendRequest

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            toolbar.updatePadding(left = insets.left, top = insets.top)
            content.updatePadding(left = insets.left, right = insets.right)
            compat
        }

        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        vm.byId(PublicKey(requireStringArg(FRIEND_REQUEST_PUBLIC_KEY))).observe(viewLifecycleOwner) {
            friendRequest = it
            from.text = it.publicKey
            message.text = it.message
            reject.isEnabled = true
            accept.isEnabled = true
        }

        accept.setOnClickListener {
            vm.accept(friendRequest)
            findNavController().popBackStack()
        }

        reject.setOnClickListener {
            vm.reject(friendRequest)
            findNavController().popBackStack()
        }
    }
}
