// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.DialogReceiveShareBinding
import ltd.evilcorp.atox.truncated
import ltd.evilcorp.atox.ui.contactlist.ContactAdapter
import ltd.evilcorp.core.vo.Contact

private const val SHARE_TEXT_PREVIEW_LENGTH = 128

class ReceiveShareDialog(
    ctx: Context,
    private var contacts: List<Contact>,
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
            it.adapter = ContactAdapter(layoutInflater, context).apply {
                contacts = this@ReceiveShareDialog.contacts
                notifyDataSetChanged()
            }
            it.setOnItemClickListener { _, _, position, _ ->
                val contact = binding.contacts.getItemAtPosition(position) as Contact
                contactSelectedFunc(contact)
                dismiss()
            }
        }
    }

    fun setContacts(contacts: List<Contact>) {
        this.contacts = contacts
        (binding.contacts.adapter as ContactAdapter?)?.let {
            it.contacts = this.contacts
            it.notifyDataSetChanged()
        }
    }
}
