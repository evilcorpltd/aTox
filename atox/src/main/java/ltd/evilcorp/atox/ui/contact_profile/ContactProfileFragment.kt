package ltd.evilcorp.atox.ui.contact_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.contact_profile_fragment.view.*
import kotlinx.android.synthetic.main.profile_image_layout.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.tox.PublicKey
import ltd.evilcorp.atox.ui.chat.CONTACT_PUBLIC_KEY
import ltd.evilcorp.atox.ui.colorByStatus
import ltd.evilcorp.atox.ui.setAvatarFromContact
import ltd.evilcorp.atox.vmFactory

class ContactProfileFragment : Fragment() {
    private val viewModel: ContactProfileViewModel by viewModels { vmFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.contact_profile_fragment, container, false).apply {
        toolbar.setNavigationIcon(R.drawable.back)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        viewModel.publicKey = PublicKey(requireStringArg(CONTACT_PUBLIC_KEY))
        viewModel.contact.observe(viewLifecycleOwner) { contact ->
            headerMainText.text = contact.name
            setAvatarFromContact(profileImage, contact)
            statusIndicator.setColorFilter(colorByStatus(resources, contact))

            contactPublicKey.text = contact.publicKey
            contactName.text = contact.name
            contactStatusMessage.text = contact.statusMessage
        }
    }
}
