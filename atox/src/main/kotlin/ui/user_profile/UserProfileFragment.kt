package ltd.evilcorp.atox.ui.user_profile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentUserProfileBinding
import ltd.evilcorp.atox.setUpFullScreenUi
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.ui.StatusDialog
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.UserStatus

private const val TOX_MAX_NAME_LENGTH = 128
private const val TOX_MAX_STATUS_MESSAGE_LENGTH = 1007

class UserProfileFragment : BaseFragment<FragmentUserProfileBinding>(FragmentUserProfileBinding::inflate) {
    private val vm: UserProfileViewModel by viewModels { vmFactory }
    private lateinit var currentStatus: UserStatus

    private fun colorFromStatus(status: UserStatus) = when (status) {
        UserStatus.None -> ResourcesCompat.getColor(resources, R.color.statusAvailable, null)
        UserStatus.Away -> ResourcesCompat.getColor(resources, R.color.statusAway, null)
        UserStatus.Busy -> ResourcesCompat.getColor(resources, R.color.statusBusy, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        view.setUpFullScreenUi { _, insets ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@setUpFullScreenUi insets
            profileCollapsingToolbar.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )
            profileToolbar.updatePadding(top = insets.systemWindowInsetTop)
            mainSection.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )
            insets
        }

        profileToolbar.apply {
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }

        vm.user.observe(viewLifecycleOwner) { user ->
            currentStatus = user.status

            userName.text = user.name
            userStatusMessage.text = user.statusMessage
            userStatus.setColorFilter(colorFromStatus(user.status))
        }

        userToxId.text = vm.toxId.string()
        profileShareId.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, vm.toxId.string())
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.tox_id_share)))
        }

        profileOptions.profileChangeNickname.setOnClickListener {
            val nameEdit = EditText(requireContext()).apply {
                text.append(binding.userName.text)
                filters = arrayOf(InputFilter.LengthFilter(TOX_MAX_NAME_LENGTH))
                setSingleLine()
            }
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.name)
                .setView(nameEdit)
                .setPositiveButton(R.string.update) { _, _ ->
                    vm.setName(nameEdit.text.toString())
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
        }

        profileOptions.profileChangeStatusText.setOnClickListener {
            val statusMessageEdit =
                EditText(requireContext()).apply {
                    text.append(binding.userStatusMessage.text)
                    filters = arrayOf(InputFilter.LengthFilter(TOX_MAX_STATUS_MESSAGE_LENGTH))
                }
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.status_message)
                .setView(statusMessageEdit)
                .setPositiveButton(R.string.update) { _, _ ->
                    vm.setStatusMessage(statusMessageEdit.text.toString())
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
        }

        profileOptions.profileChangeStatus.setOnClickListener {
            StatusDialog(requireContext(), currentStatus) { status -> vm.setStatus(status) }.show()
        }
    }
}
