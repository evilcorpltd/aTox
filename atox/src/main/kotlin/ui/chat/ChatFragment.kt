package ltd.evilcorp.atox.ui.chat

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import java.net.URLConnection
import java.text.DateFormat
import java.util.Locale
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
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.isComplete
import ltd.evilcorp.domain.tox.PublicKey

const val CONTACT_PUBLIC_KEY = "publicKey"
private const val REQUEST_CODE_FT_FILE = 1234
private const val TAG = "ChatFragment"

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private val viewModel: ChatViewModel by viewModels { vmFactory }

    private lateinit var contactPubKey: String
    private var contactName = ""
    private var contactOnline = false
    private var selectedFt: Int = Int.MIN_VALUE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = view.run {
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

        viewModel.contact.observe(viewLifecycleOwner) {
            contactName = it.name
            contactOnline = it.connectionStatus != ConnectionStatus.None

            title.text = contactName
            subtitle.text = when {
                it.typing -> getString(R.string.contact_typing)
                it.lastMessage == 0L -> getString(R.string.never)
                else ->
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(it.lastMessage) // TODO(robinlinden): Replace with last seen.
            }.toLowerCase(Locale.getDefault())

            statusIndicator.setColorFilter(colorByStatus(resources, it))
            setAvatarFromContact(profileImage, it)
        }

        val adapter = ChatAdapter(layoutInflater, resources)
        messages.adapter = adapter
        registerForContextMenu(messages)
        viewModel.messages.observe(viewLifecycleOwner) {
            adapter.messages = it
            adapter.notifyDataSetChanged()
        }

        viewModel.fileTransfers.observe(viewLifecycleOwner) {
            adapter.fileTransfers = it
            adapter.notifyDataSetChanged()
        }

        messages.setOnItemClickListener { _, view, position, _ ->
            when (view.id) {
                R.id.accept -> {
                    selectedFt = adapter.messages[position].correlationId
                    Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/octet-stream"
                        putExtra(Intent.EXTRA_TITLE, adapter.messages[position].message)
                    }.also {
                        startActivityForResult(it, REQUEST_CODE_FT_FILE)
                    }
                }
                R.id.reject -> viewModel.rejectFt(adapter.messages[position].correlationId)
                R.id.fileTransfer -> {
                    val id = adapter.messages[position].correlationId
                    val ft = adapter.fileTransfers.find { it.id == id } ?: return@setOnItemClickListener
                    if (!ft.isComplete()) return@setOnItemClickListener
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(ft.destination)
                        type = URLConnection.guessContentTypeFromName(ft.fileName)
                    }.also {
                        try {
                            startActivity(it)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                requireContext(),
                                getString(
                                    R.string.mimetype_handler_not_found,
                                    URLConnection.guessContentTypeFromName(ft.fileName)
                                ),
                                Toast.LENGTH_LONG
                            ).show()
                            Log.i(TAG, e.toString())
                        }
                    }
                }
            }
        }

        registerForContextMenu(send)
        send.setOnClickListener {
            sendMessage()
        }

        send.isEnabled = false
        send.setColorFilter(ResourcesCompat.getColor(resources, android.R.color.darker_gray, null))

        outgoingMessage.doAfterTextChanged {
            viewModel.setTyping(outgoingMessage.text.isNotEmpty())
            updateSendButton(this@run)
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
                val info = menuInfo as AdapterView.AdapterContextMenuInfo
                val message = messages.adapter.getItem(info.position) as Message
                if (message.type != MessageType.FileTransfer) {
                    requireActivity().menuInflater.inflate(R.menu.chat_message_context_menu, menu)
                }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_FT_FILE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    viewModel.acceptFt(selectedFt, data.data as Uri)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun sendMessage() {
        val message = outgoingMessage.text.toString()
        outgoingMessage.text.clear()
        if (contactOnline) {
            viewModel.send(message, MessageType.Normal)
        } else {
            viewModel.queue(message, MessageType.Normal)
        }
    }

    private fun sendAction() {
        val message = outgoingMessage.text.toString()
        outgoingMessage.text.clear()
        if (contactOnline) {
            viewModel.send(message, MessageType.Action)
        } else {
            viewModel.queue(message, MessageType.Action)
        }
    }

    private fun updateSendButton(layout: View) {
        layout.send.isEnabled = layout.outgoingMessage.text.isNotEmpty()
        send.setColorFilter(
            ResourcesCompat.getColor(
                resources,
                if (layout.send.isEnabled) R.color.colorPrimary else android.R.color.darker_gray,
                null
            )
        )
    }
}
