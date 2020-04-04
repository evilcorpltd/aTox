package ltd.evilcorp.atox.ui.chat

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.profile_image_layout.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.setUpFullScreenUi
import ltd.evilcorp.atox.ui.colorByStatus
import ltd.evilcorp.atox.ui.setAvatarFromContact
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.domain.tox.PublicKey
import java.text.DateFormat
import java.util.*

const val CONTACT_PUBLIC_KEY = "publicKey"

class ChatFragment : Fragment() {
    private val viewModel: ChatViewModel by viewModels { vmFactory }

    private lateinit var contactPubKey: String
    private var contactName = ""
    private var contactOnline = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_chat, container, false).apply {
        contactPubKey = requireStringArg(CONTACT_PUBLIC_KEY)
        viewModel.setActiveChat(PublicKey(contactPubKey))

        setUpFullScreenUi { _, insets ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@setUpFullScreenUi insets
            toolbar.updatePadding(
                left = insets.systemWindowInsetLeft,
                top = insets.systemWindowInsetTop,
                right = insets.systemWindowInsetRight
            )
            bottomBar.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight,
                bottom = insets.systemWindowInsetBottom
            )
            messages.updatePadding(
                left = insets.systemWindowInsetLeft,
                right = insets.systemWindowInsetRight
            )
            insets
        }

        toolbar.setNavigationIcon(R.drawable.back)
        toolbar.setNavigationOnClickListener {
            viewModel.setActiveChat(PublicKey(""))
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

        contactHeader.setOnClickListener {
            findNavController().navigate(
                R.id.action_chatFragment_to_contactProfileFragment,
                Bundle().apply { putString(CONTACT_PUBLIC_KEY, contactPubKey) }
            )
        }

        viewModel.contact.observe(viewLifecycleOwner, Observer {
            contactName = it.name
            contactOnline = it.connectionStatus != ConnectionStatus.None

            title.text = contactName
            subtitle.text = when {
                it.typing -> getString(R.string.contact_typing)
                it.lastMessage == 0L -> getString(R.string.never)
                else -> DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(it.lastMessage) // TODO(robinlinden): Replace with last seen.
            }.toLowerCase(Locale.getDefault())

            statusIndicator.setColorFilter(colorByStatus(resources, it))
            setAvatarFromContact(profileImage, it)

            updateSendButton(this)
        })

        val adapter = ChatAdapter(inflater, resources)
        messages.adapter = adapter
        registerForContextMenu(messages)
        viewModel.messages.observe(viewLifecycleOwner, Observer {
            adapter.messages = it
            adapter.notifyDataSetChanged()
        })

        registerForContextMenu(send)
        send.setOnClickListener {
            sendMessage()
        }

        send.isEnabled = false
        send.setColorFilter(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))

        outgoingMessage.doAfterTextChanged {
            viewModel.setTyping(outgoingMessage.text.isNotEmpty())
            updateSendButton(this@apply)
        }
    }

    override fun onPause() {
        viewModel.setActiveChat(PublicKey(""))
        super.onPause()
    }

    override fun onResume() {
        viewModel.setActiveChat(PublicKey(contactPubKey))
        viewModel.setTyping(outgoingMessage.text.isNotEmpty())
        super.onResume()
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
            R.id.send -> {
                requireActivity().menuInflater.inflate(R.menu.chat_send_long_press_menu, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.copy -> {
                val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
                val clipboard = requireActivity().getSystemService<ClipboardManager>()!!
                val message = messages.adapter.getItem(info.position) as Message
                clipboard.setPrimaryClip(ClipData.newPlainText(getText(R.string.message), message.message))

                Toast.makeText(requireContext(), getText(R.string.copied), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.send_action -> {
                sendAction()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun sendMessage() {
        val message = outgoingMessage.text.toString()
        outgoingMessage.text.clear()
        viewModel.sendMessage(message)
    }

    private fun sendAction() {
        val message = outgoingMessage.text.toString()
        outgoingMessage.text.clear()
        viewModel.sendAction(message)
    }

    private fun updateSendButton(layout: View) {
        layout.send.isEnabled =
            layout.outgoingMessage.text.isNotEmpty() && layout.outgoingMessage.error.isNullOrEmpty() && contactOnline
        send.setColorFilter(
            ResourcesCompat.getColor(
                resources,
                if (layout.send.isEnabled) R.color.colorPrimary else android.R.color.darker_gray,
                null
            )
        )
    }
}
