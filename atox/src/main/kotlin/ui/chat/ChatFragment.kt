package ltd.evilcorp.atox.ui.chat

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import java.io.File
import java.net.URLConnection
import java.text.DateFormat
import java.util.Locale
import ltd.evilcorp.atox.BuildConfig
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.databinding.FragmentChatBinding
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.truncated
import ltd.evilcorp.atox.ui.BaseFragment
import ltd.evilcorp.atox.ui.colorByStatus
import ltd.evilcorp.atox.ui.setAvatarFromContact
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.FileTransfer
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.isComplete
import ltd.evilcorp.domain.tox.PublicKey

const val CONTACT_PUBLIC_KEY = "publicKey"
private const val REQUEST_CODE_FT_EXPORT = 1234
private const val REQUEST_CODE_ATTACH = 5678
private const val MAX_CONFIRM_DELETE_STRING_LENGTH = 20

class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {
    private val viewModel: ChatViewModel by viewModels { vmFactory }

    private lateinit var contactPubKey: String
    private var contactName = ""
    private var selectedFt: Int = Int.MIN_VALUE
    private var fts: List<FileTransfer> = listOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = binding.run {
        contactPubKey = requireStringArg(CONTACT_PUBLIC_KEY)
        viewModel.setActiveChat(PublicKey(contactPubKey))

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            toolbar.updatePadding(left = insets.left, top = insets.top, right = insets.right)
            bottomBar.updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)
            messages.updatePadding(left = insets.left, right = insets.right)
            compat
        }

        toolbar.setNavigationIcon(R.drawable.back)
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

        toolbar.inflateMenu(R.menu.chat_options_menu)
        toolbar.menu.findItem(R.id.call).isEnabled = !viewModel.inCall()
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
                R.id.call -> {
                    findNavController().navigate(
                        R.id.action_chatFragment_to_callFragment,
                        bundleOf(CONTACT_PUBLIC_KEY to contactPubKey)
                    )
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }

        contactHeader.setOnClickListener {
            findNavController().navigate(
                R.id.action_chatFragment_to_contactProfileFragment,
                bundleOf(CONTACT_PUBLIC_KEY to contactPubKey)
            )
        }

        viewModel.contact.observe(viewLifecycleOwner) {
            contactName = it.name
            viewModel.contactOnline = it.connectionStatus != ConnectionStatus.None

            title.text = contactName
            // TODO(robinlinden): Replace last message with last seen.
            subtitle.text = when {
                it.typing -> getString(R.string.contact_typing)
                it.lastMessage == 0L -> getString(R.string.never)
                else -> DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(it.lastMessage)
            }.toLowerCase(Locale.getDefault())

            profileLayout.statusIndicator.setColorFilter(colorByStatus(resources, it))
            setAvatarFromContact(profileLayout.profileImage, it)

            if (it.draftMessage.isNotEmpty() && outgoingMessage.text.isEmpty()) {
                outgoingMessage.setText(it.draftMessage)
                viewModel.clearDraft()
            }

            updateActions()
        }

        val adapter = ChatAdapter(layoutInflater, resources)
        messages.adapter = adapter
        registerForContextMenu(messages)
        viewModel.messages.observe(viewLifecycleOwner) {
            adapter.messages = it
            adapter.notifyDataSetChanged()
        }

        viewModel.fileTransfers.observe(viewLifecycleOwner) {
            fts = it
            adapter.fileTransfers = it
            adapter.notifyDataSetChanged()
        }

        messages.setOnItemClickListener { _, view, position, _ ->
            when (view.id) {
                R.id.accept -> viewModel.acceptFt(adapter.messages[position].correlationId)
                R.id.reject, R.id.cancel -> viewModel.rejectFt(adapter.messages[position].correlationId)
                R.id.fileTransfer -> {
                    val id = adapter.messages[position].correlationId
                    val ft = adapter.fileTransfers.find { it.id == id } ?: return@setOnItemClickListener
                    if (ft.outgoing) return@setOnItemClickListener
                    if (!ft.isComplete()) return@setOnItemClickListener
                    if (!ft.destination.startsWith("file://")) return@setOnItemClickListener
                    val contentType = URLConnection.guessContentTypeFromName(ft.fileName)
                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                        File(Uri.parse(ft.destination).path!!)
                    )
                    val shareIntent = Intent(Intent.ACTION_VIEW).apply {
                        putExtra(Intent.EXTRA_TITLE, ft.fileName)
                        setDataAndType(uri, contentType)
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    try {
                        startActivity(Intent.createChooser(shareIntent, null))
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.mimetype_handler_not_found, contentType),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        registerForContextMenu(send)
        send.setOnClickListener { send(MessageType.Normal) }

        attach.setOnClickListener {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }.also {
                startActivityForResult(it, REQUEST_CODE_ATTACH)
            }
        }

        outgoingMessage.doAfterTextChanged {
            viewModel.setTyping(outgoingMessage.text.isNotEmpty())
            updateActions()
        }

        updateActions()
    }

    override fun onPause() {
        viewModel.setDraft(binding.outgoingMessage.text.toString())
        viewModel.setActiveChat(PublicKey(""))
        super.onPause()
    }

    override fun onResume() = binding.run {
        viewModel.setActiveChat(PublicKey(contactPubKey))
        viewModel.setTyping(outgoingMessage.text.isNotEmpty())
        super.onResume()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) = binding.run {
        super.onCreateContextMenu(menu, v, menuInfo)
        v.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0f, 0f, 0))
        val inflater = requireActivity().menuInflater
        when (v.id) {
            R.id.messages -> {
                val info = menuInfo as AdapterView.AdapterContextMenuInfo
                val message = messages.adapter.getItem(info.position) as Message
                when (message.type) {
                    MessageType.Action, MessageType.Normal -> inflater.inflate(R.menu.chat_message_context_menu, menu)
                    MessageType.FileTransfer -> {
                        inflater.inflate(R.menu.ft_message_context_menu, menu)
                        val ft = fts.find { it.id == message.correlationId } ?: return
                        if (!ft.isComplete() || ft.outgoing || !ft.destination.startsWith("file://")) {
                            menu.findItem(R.id.export).isVisible = false
                        }
                    }
                }
            }
            R.id.send -> requireActivity().menuInflater.inflate(R.menu.chat_send_long_press_menu, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = binding.run {
        return when (item.itemId) {
            R.id.copy -> {
                val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
                val clipboard = requireActivity().getSystemService<ClipboardManager>()!!
                val message = messages.adapter.getItem(info.position) as Message
                clipboard.setPrimaryClip(ClipData.newPlainText(getText(R.string.message), message.message))

                Toast.makeText(requireContext(), getText(R.string.copied), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.delete -> {
                val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
                val message = messages.adapter.getItem(info.position) as Message

                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.clear_history)
                    .setMessage(
                        getString(
                            R.string.delete_message_confirm,
                            message.message.truncated(MAX_CONFIRM_DELETE_STRING_LENGTH)
                        )
                    )
                    .setPositiveButton(R.string.delete) { _, _ ->
                        viewModel.delete(message)
                    }
                    .setNegativeButton(R.string.cancel, null).show()
                true
            }
            R.id.send_action -> {
                send(MessageType.Action)
                true
            }
            R.id.export -> {
                val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
                val message = messages.adapter.getItem(info.position) as Message
                selectedFt = message.correlationId
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_TITLE, message.message)
                }.let {
                    startActivityForResult(it, REQUEST_CODE_FT_EXPORT)
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Runs before onResume, so add back some required state..
        viewModel.setActiveChat(PublicKey(contactPubKey))
        when (requestCode) {
            REQUEST_CODE_FT_EXPORT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    viewModel.exportFt(selectedFt, data.data as Uri)
                }
            }
            REQUEST_CODE_ATTACH -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (data.data != null) {
                        // Single file.
                        viewModel.createFt(data.data as Uri)
                    } else if (data.clipData != null) {
                        // Multiple files.
                        val clipData = data.clipData ?: return
                        for (i in 0 until clipData.itemCount) {
                            viewModel.createFt(clipData.getItemAt(i).uri)
                        }
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun send(type: MessageType) = binding.run {
        viewModel.clearDraft()
        viewModel.send(outgoingMessage.text.toString(), type)
        outgoingMessage.text.clear()
    }

    private fun updateActions() = binding.run {
        send.visibility = if (outgoingMessage.text.isEmpty()) View.GONE else View.VISIBLE
        attach.visibility = if (send.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        attach.isEnabled = viewModel.contactOnline
        attach.setColorFilter(
            ResourcesCompat.getColor(
                resources,
                if (attach.isEnabled) R.color.colorPrimary else android.R.color.darker_gray,
                null
            )
        )
    }
}
