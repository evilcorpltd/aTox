// SPDX-FileCopyrightText: 2021-2022 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.DialogReceiveShareBinding
import ltd.evilcorp.atox.truncated
import ltd.evilcorp.atox.ui.contactlist.ContactAdapter
import ltd.evilcorp.core.vo.Contact

private const val SHARE_TEXT_PREVIEW_LENGTH = 128

class ReceiveShareDialogFragment(
    private val contacts: LiveData<List<Contact>>,
    private val sharePreview: String,
    private val onContactSelected: (Contact) -> Unit,
    private val onDialogDismissed: () -> Unit,
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = ReceiveShareDialog(requireContext(), sharePreview, onContactSelected)
        contacts.observe(this) { dialog.setContacts(it.sortedByDescending(::contactListSorter)) }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDialogDismissed()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onDialogDismissed()
    }
}

private class ReceiveShareDialog(
    ctx: Context,
    private val sharePreview: String,
    private val contactSelectedFunc: (Contact) -> Unit,
) : Dialog(ctx, R.style.DialogSlideAnimation) {
    private var _binding: DialogReceiveShareBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        _binding = DialogReceiveShareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.sharingText.text = String.format("%s", sharePreview.truncated(SHARE_TEXT_PREVIEW_LENGTH))

        binding.contacts.let {
            it.adapter = ContactAdapter(layoutInflater, context)
            it.setOnItemClickListener { _, _, position, _ ->
                val contact = binding.contacts.getItemAtPosition(position) as Contact
                contactSelectedFunc(contact)
                dismiss()
            }
        }
    }

    fun setContacts(contacts: List<Contact>) {
        (binding.contacts.adapter as ContactAdapter?)?.let {
            it.contacts = contacts
            it.notifyDataSetChanged()
        }
    }
}
