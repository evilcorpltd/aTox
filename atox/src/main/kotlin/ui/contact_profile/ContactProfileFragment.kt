package ltd.evilcorp.atox.ui.contact_profile

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentContactProfileBinding
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.setUpFullScreenUi
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.atox.ui.colorByStatus
import ltd.evilcorp.atox.ui.setAvatarFromContact
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.domain.tox.PublicKey

class ContactProfileFragment : BaseFragment<FragmentContactProfileBinding>(FragmentContactProfileBinding::inflate) {
    private val viewModel: ContactProfileViewModel by viewModels { vmFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        view.setUpFullScreenUi { _, insets ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@setUpFullScreenUi insets
            appBar.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
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

        viewModel.publicKey = PublicKey(requireStringArg(CONTACT_PUBLIC_KEY))
        viewModel.contact.observe(viewLifecycleOwner) { contact ->
            headerMainText.text = contact.name
            setAvatarFromContact(profileLayout.profileImage, contact)
            profileLayout.statusIndicator.setColorFilter(colorByStatus(resources, contact))

            contactPublicKey.text = contact.publicKey
            contactName.text = contact.name
            contactStatusMessage.text = contact.statusMessage
        }
    }
}
