package ltd.evilcorp.atox.ui.addcontact

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.add_contact_fragment.view.*
import ltd.evilcorp.atox.R

class AddContactFragment : Fragment() {

    companion object {
        fun newInstance() = AddContactFragment()
    }

    private lateinit var viewModel: AddContactViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.add_contact_fragment, container, false)

        layout.toxId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val content = s?.toString()
                // TODO(robinlinden): Checksum error check.
                layout.toxId.error = if (content?.length == 76) null else getString(R.string.tox_id_error_length)
                layout.add.isEnabled = layout.toxId.error == null
            }
        })

        layout.message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val content = s?.toString()
                layout.message.error =
                    if (content?.length != 0) null else getString(R.string.add_contact_message_error_empty)
                layout.add.isEnabled = layout.message.error == null
            }
        })

        layout.add.setOnClickListener {
            viewModel.addContact(layout.toxId.text.toString(), layout.message.text.toString())
            requireActivity().finish()
        }

        layout.add.isEnabled = false

        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AddContactViewModel::class.java)
    }
}
