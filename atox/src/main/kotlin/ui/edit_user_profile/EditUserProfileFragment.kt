package ltd.evilcorp.atox.ui.edit_user_profile

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentEditUserProfileBinding
import ltd.evilcorp.atox.ui.*
import ltd.evilcorp.atox.ui.AvatarMaker
import ltd.evilcorp.atox.ui.SizeUnit
import ltd.evilcorp.atox.ui.edit_text_value_dialog.EditTextValueDialog
import ltd.evilcorp.atox.ui.isNightMode
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.UserStatus
import kotlin.math.min

private const val TOX_MAX_NAME_LENGTH = 128
private const val TOX_MAX_STATUS_MESSAGE_LENGTH = 1007

private const val AVATAR_IMAGE_TO_SCREEN_RATIO = 1f/3

class EditUserProfileFragment : BaseFragment<FragmentEditUserProfileBinding>(FragmentEditUserProfileBinding::inflate) {
    private val vm: EditUserProfileViewModel by viewModels { vmFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            toolbar.updatePadding(left = insets.left, top = insets.top)
            mainSection.updatePadding(left = insets.left, right = insets.right)
            compat
        }

        // Inflating views according to Day/Night theme
        if (isNightMode(requireContext())) {
            toolbar.setNavigationIcon(R.drawable.ic_back_white)

            ContextCompat.getColorStateList(requireContext(), R.color.box_stroke_color_night)?.run {
                editStatus.setBoxStrokeColorStateList(this)
            }
            editStatus.defaultHintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.hint_text_color_night)
            editStatus.setEndIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.trailing_icon_color_night))
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_back_black)
        }

        toolbar.setNavigationOnClickListener {
            WindowInsetsControllerCompat(requireActivity().window, view)
                .hide(WindowInsetsCompat.Type.ime())
            activity?.onBackPressed()
        }

        // Setting the adapter for edit status
        val statusList = resources.getStringArray(R.array.status_list)
        val adapter = StatusArrayAdapter(requireContext(), R.layout.edit_status_item, R.id.item_text, statusList)
        editStatusText.setAdapter(adapter)

        // Setting the icon and the status according to the chosen status
        editStatusText.doOnTextChanged { text, _, _, _ ->
            when (text.toString()) {
                getString(R.string.status_available) -> {
                    editStatus.setStartIconTintList(
                        ResourcesCompat.getColorStateList(resources, R.color.status_available_color_list, null)
                    )
                    vm.setStatus(UserStatus.None)
                }
                getString(R.string.status_away) -> {
                    editStatus.setStartIconTintList(
                        ResourcesCompat.getColorStateList(resources, R.color.status_away_color_list, null)
                    )
                    vm.setStatus(UserStatus.Away)
                }
                getString(R.string.status_busy) -> {
                    editStatus.setStartIconTintList(
                        ResourcesCompat.getColorStateList(resources, R.color.status_busy_color_list, null)
                    )
                    vm.setStatus(UserStatus.Busy)
                }
            }
        }

        // Getting the avatar image side value according to the screen resolution
        val metrics = resources.displayMetrics
        val side = (min(metrics.widthPixels, metrics.heightPixels) * AVATAR_IMAGE_TO_SCREEN_RATIO).toInt()

        vm.user.observe(viewLifecycleOwner) { user ->
            if (vm.statusModifiedFromDropdown) {
                vm.statusModifiedFromDropdown = false
            } else {
                AvatarMaker(user).setAvatar(avatarImage, side, SizeUnit.PX)
                userName.text = user.name
                editStatusText.setText(
                    when (user.status) {
                        UserStatus.None -> getString(R.string.status_available)
                        UserStatus.Away -> getString(R.string.status_away)
                        UserStatus.Busy -> getString(R.string.status_busy)
                    },
                    false
                )
                statusMessage.text = user.statusMessage
            }
        }

        editName.setOnClickListener {
            EditTextValueDialog(
                title = getString(R.string.edit_name),
                hint = getString(R.string.name),
                defaultValue = userName.text.toString(),
                filters = arrayOf(InputFilter.LengthFilter(TOX_MAX_NAME_LENGTH))
            ) {
                vm.setName(it)
            }.show(requireActivity().supportFragmentManager, EditTextValueDialog.TAG)
        }

        editStatusMessage.setOnClickListener {
            EditTextValueDialog(
                title = getString(R.string.edit_status_message),
                hint = getString(R.string.status_message),
                defaultValue = statusMessage.text.toString(),
                singleLine = false,
                filters = arrayOf(InputFilter.LengthFilter(TOX_MAX_STATUS_MESSAGE_LENGTH))
            ) {
                vm.setStatusMessage(it)
            }.show(requireActivity().supportFragmentManager, EditTextValueDialog.TAG)
        }

    }
}
