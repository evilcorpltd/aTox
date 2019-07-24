package ltd.evilcorp.atox.ui.chat

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.android.synthetic.main.chat_fragment.view.*
import kotlinx.android.synthetic.main.profile_image_layout.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.ui.MessagesAdapter
import ltd.evilcorp.atox.ui.colorByStatus
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.atox.vo.ConnectionStatus
import ltd.evilcorp.atox.vo.Message

const val CONTACT_PUBLIC_KEY = "publicKey"

class ChatFragment : Fragment() {
    private val viewModel: ChatViewModel by viewModels { vmFactory }

    private var contactName = ""
    private var contactOnline = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.chat_fragment, container, false).apply {
        viewModel.publicKey = arguments!!.getString(CONTACT_PUBLIC_KEY)!!

        toolbar.setNavigationIcon(R.drawable.back)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        toolbar.inflateMenu(R.menu.chat_options_menu)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.clear_history -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.clear_history)
                        .setMessage(getString(R.string.clear_history_confirm, contactName))
                        .setPositiveButton(R.string.clear_history) { _, _ ->
                            Toast.makeText(requireContext(), R.string.clear_history_cleared, Toast.LENGTH_LONG).show()
                            viewModel.clearHistory()
                        }
                        .setNegativeButton(R.string.cancel, null).show()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }

        viewModel.contact.observe(viewLifecycleOwner, Observer {
            contactName = it.name
            contactOnline = it.connectionStatus != ConnectionStatus.NONE

            title.text = contactName
            subtitle.text = if (it.typing) {
                getString(R.string.contact_typing)
            } else {
                // TODO(robinlinden): Replace with last seen.
                it.lastMessage
            }.toLowerCase()
            statusIndicator.setColorFilter(colorByStatus(resources, it))

            updateSendButton(this)
        })

        val adapter = MessagesAdapter(inflater)
        messages.adapter = adapter
        registerForContextMenu(messages)
        viewModel.messages.observe(viewLifecycleOwner, Observer {
            adapter.messages = it
            adapter.notifyDataSetChanged()
        })

        send.setOnClickListener {
            val message = outgoingMessage.text.toString()
            outgoingMessage.text.clear()
            viewModel.sendMessage(message)
        }

        send.isEnabled = false

        outgoingMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSendButton(this@apply)
            }
        })
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        when (v.id) {
            R.id.messages -> {
                requireActivity().menuInflater.inflate(R.menu.chat_message_context_menu, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        return when (item.itemId) {
            R.id.copy -> {
                val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val message = messages.adapter.getItem(info.position) as Message
                clipboard.primaryClip = ClipData.newPlainText(getText(R.string.message), message.message)

                Toast.makeText(requireContext(), getText(R.string.copied), Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun updateSendButton(layout: View) {
        layout.send.isEnabled = layout.outgoingMessage.text.isNotEmpty() && contactOnline
    }
}
