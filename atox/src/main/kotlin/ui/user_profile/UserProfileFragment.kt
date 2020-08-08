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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.android.synthetic.main.fragment_user_profile.view.*
import kotlinx.android.synthetic.main.profile_options.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.setUpFullScreenUi
import ltd.evilcorp.atox.ui.StatusDialog
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.UserStatus

private const val TOX_MAX_NAME_LENGTH = 128
private const val TOX_MAX_STATUS_MESSAGE_LENGTH = 1007

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {
    private val vm: UserProfileViewModel by viewModels { vmFactory }
    private lateinit var currentStatus: UserStatus

    private fun colorFromStatus(status: UserStatus) = when (status) {
        UserStatus.None -> ResourcesCompat.getColor(resources, R.color.statusAvailable, null)
        UserStatus.Away -> ResourcesCompat.getColor(resources, R.color.statusAway, null)
        UserStatus.Busy -> ResourcesCompat.getColor(resources, R.color.statusBusy, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = view.run {
        setUpFullScreenUi { _, insets ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@setUpFullScreenUi insets
            profile_collapsing_toolbar.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )
            profile_toolbar.updatePadding(top = insets.systemWindowInsetTop)
            main_section.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )
            insets
        }

        profile_toolbar.apply {
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }

        vm.user.observe(viewLifecycleOwner) { user ->
            currentStatus = user.status

            user_name.text = user.name
            user_status_message.text = user.statusMessage
            user_status.setColorFilter(colorFromStatus(user.status))
        }

        user_tox_id.text = vm.toxId.string()
        profile_share_id.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, vm.toxId.string())
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.tox_id_share)))
        }

        profile_change_nickname.setOnClickListener {
            val nameEdit = EditText(requireContext()).apply {
                text.append(this@UserProfileFragment.user_name.text)
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

        profile_change_status_text.setOnClickListener {
            val statusMessageEdit =
                EditText(requireContext()).apply {
                    text.append(this@UserProfileFragment.user_status_message.text)
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

        profile_change_status.setOnClickListener {
            StatusDialog(context, currentStatus) { status -> vm.setStatus(status) }.show()
        }
    }
}
