package ltd.evilcorp.atox.ui.addcontact

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.add_contact_fragment.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.tox.ToxID
import ltd.evilcorp.atox.vmFactory

class AddContactFragment : Fragment() {
    private val viewModel: AddContactViewModel by viewModels { vmFactory }

    private var toxIdValid: Boolean = false
    private var messageValid: Boolean = true

    private fun isAddAllowed(): Boolean = toxIdValid && messageValid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.add_contact_fragment, container, false).apply {
        toxId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val content = s?.toString() ?: ""
                // TODO(robinlinden): Checksum error check.
                toxId.error = if (content.length == 76) null else getString(R.string.tox_id_error_length)

                toxIdValid = toxId.error == null
                add.isEnabled = isAddAllowed()
            }
        })

        message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val content = s?.toString() ?: ""
                message.error = if (content.isNotEmpty()) null else getString(R.string.add_contact_message_error_empty)

                messageValid = message.error == null
                add.isEnabled = isAddAllowed()
            }
        })

        add.setOnClickListener {
            viewModel.addContact(ToxID(toxId.text.toString()), message.text.toString())
            findNavController().popBackStack()
        }

        add.isEnabled = false
    }
}
