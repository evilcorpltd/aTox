// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
// SPDX-FileCopyrightText: 2021-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.contactprofile

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentContactProfileBinding
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.PublicKey

class ContactProfileFragment : BaseFragment<FragmentContactProfileBinding>(FragmentContactProfileBinding::inflate) {
    private val viewModel: ContactProfileViewModel by viewModels { vmFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            appBar.updatePadding(left = insets.left, right = insets.right)
            content.updatePadding(left = insets.left, right = insets.right)
            compat
        }

        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewModel.publicKey = PublicKey(requireStringArg(CONTACT_PUBLIC_KEY))
        viewModel.contact.observe(viewLifecycleOwner) { contact ->
            contact.name = contact.name.ifEmpty { getString(R.string.contact_default_name) }

            headerMainText.text = contact.name
            avatarImageView.setFrom(contact)

            contactPublicKey.text = contact.publicKey
            contactName.text = contact.name
            contactStatusMessage.text = contact.statusMessage
            contactConnectionStatus.text = when (contact.connectionStatus) {
                ConnectionStatus.None -> getText(R.string.atox_offline)
                ConnectionStatus.TCP -> getText(R.string.atox_connected_with_tcp)
                ConnectionStatus.UDP -> getText(R.string.atox_connected_with_udp)
            }
        }
    }
}
