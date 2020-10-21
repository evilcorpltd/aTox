package ltd.evilcorp.atox.ui.friend_request

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentFriendRequestBinding
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.setUpFullScreenUi
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.domain.tox.PublicKey

const val FRIEND_REQUEST_PUBLIC_KEY = "FRIEND_REQUEST_PUBLIC_KEY"

class FriendRequestFragment : BaseFragment<FragmentFriendRequestBinding>(FragmentFriendRequestBinding::inflate) {
    private val vm: FriendRequestViewModel by viewModels { vmFactory }
    private lateinit var friendRequest: FriendRequest

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        view.setUpFullScreenUi { _, insets ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@setUpFullScreenUi insets
            toolbar.updatePadding(
                left = insets.systemWindowInsetLeft,
                top = insets.systemWindowInsetTop
            )
            content.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )
            insets
        }

        toolbar.setNavigationIcon(R.drawable.back)
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
