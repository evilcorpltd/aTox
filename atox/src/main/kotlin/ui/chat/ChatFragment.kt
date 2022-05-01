// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.chat

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.google.android.material.math.MathUtils.lerp
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
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.FileTransfer
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.isComplete
import ltd.evilcorp.domain.feature.CallState
import ltd.evilcorp.domain.tox.PublicKey
import org.kodein.di.android.x.viewmodel.viewModel

const val CONTACT_PUBLIC_KEY = "publicKey"
const val FOCUS_ON_MESSAGE_BOX = "focusOnMessageBox"
private const val MAX_CONFIRM_DELETE_STRING_LENGTH = 20

class OpenMultiplePersistableDocuments : ActivityResultContracts.OpenMultipleDocuments() {
    override fun createIntent(context: Context, input: Array<String>): Intent {
        return super.createIntent(context, input)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    }
}

class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {
    private val viewModel: ChatViewModel by viewModel()

    private lateinit var contactPubKey: String
    private var contactName = ""
    private var selectedFt: Int = Int.MIN_VALUE
    private var fts: List<FileTransfer> = listOf()

    private val exportFtLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument()) { dest ->
        if (dest == null) return@registerForActivityResult
        viewModel.exportFt(selectedFt, dest)
    }

    private val attachFilesLauncher =
        registerForActivityResult(OpenMultiplePersistableDocuments()) { files ->
            viewModel.setActiveChat(PublicKey(contactPubKey))
            for (file in files) {
                activity?.contentResolver?.takePersistableUriPermission(file, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                viewModel.createFt(file)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = binding.run {
        contactPubKey = requireStringArg(CONTACT_PUBLIC_KEY)
        viewModel.setActiveChat(PublicKey(contactPubKey))

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, compat ->
            val insets = compat.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            appBarLayout.updatePadding(left = insets.left, top = insets.top, right = insets.right)
            bottomBar.updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)
            messages.updatePadding(left = insets.left, right = insets.right)
            compat
        }

        ViewCompat.setWindowInsetsAnimationCallback(
            view,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
                var startBottom = 0
                var endBottom = 0

                override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                    val pos = IntArray(2)
                    outgoingMessage.getLocationInWindow(pos)
                    startBottom = pos[1]
                }

                override fun onStart(
                    animation: WindowInsetsAnimationCompat,
                    bounds: WindowInsetsAnimationCompat.BoundsCompat
                ): WindowInsetsAnimationCompat.BoundsCompat {
                    val pos = IntArray(2)
                    outgoingMessage.getLocationInWindow(pos)
                    endBottom = pos[1]
                    val offset = (startBottom - endBottom).toFloat()
                    messages.translationY = offset
                    bottomBar.translationY = offset

                    return bounds
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val animation = runningAnimations[0]
                    val offset = lerp((startBottom - endBottom).toFloat(), 0f, animation.interpolatedFraction)
                    messages.translationY = offset
                    bottomBar.translationY = offset
                    return insets
                }
            }
        )

        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            WindowInsetsControllerCompat(requireActivity().window, view).hide(WindowInsetsCompat.Type.ime())
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
                        .setNegativeButton(android.R.string.cancel, null).show()
                    true
                }
                R.id.call -> {
                    navigateToCallScreen()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }

        contactHeader.setOnClickListener {
            WindowInsetsControllerCompat(requireActivity().window, view).hide(WindowInsetsCompat.Type.ime())
            findNavController().navigate(
                R.id.action_chatFragment_to_contactProfileFragment,
                bundleOf(CONTACT_PUBLIC_KEY to contactPubKey)
            )
        }

        viewModel.contact.observe(viewLifecycleOwner) {
            if (it == null) {
                findNavController().popBackStack()
                return@observe
            }
            it.name = it.name.ifEmpty { getString(R.string.contact_default_name) }

            contactName = it.name
            ongoingCall.info.text = getString(R.string.in_call_with, contactName)
            viewModel.contactOnline = it.connectionStatus != ConnectionStatus.None

            title.text = contactName
            // TODO(robinlinden): Replace last message with last seen.
            subtitle.text = when {
                it.typing -> getString(R.string.contact_typing)
                it.lastMessage == 0L -> getString(R.string.never)
                else -> DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(it.lastMessage)
            }.lowercase(Locale.getDefault())

            avatarImageView.setFrom(it)

            if (it.draftMessage.isNotEmpty() && outgoingMessage.text.isEmpty()) {
                outgoingMessage.setText(it.draftMessage)
                viewModel.clearDraft()
            }

            updateActions()
        }

        viewModel.callState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CallAvailability.Unavailable -> {
                    toolbar.menu.findItem(R.id.call).title = getString(R.string.call)
                    toolbar.menu.findItem(R.id.call).isEnabled = false
                }
                CallAvailability.Available -> {
                    toolbar.menu.findItem(R.id.call).title = getString(R.string.call)
                    toolbar.menu.findItem(R.id.call).isEnabled = true
                }
                CallAvailability.Active -> {
                    toolbar.menu.findItem(R.id.call).title = getString(R.string.ongoing_call)
                    toolbar.menu.findItem(R.id.call).isEnabled = true
                }
                null -> {}
            }
        }

        viewModel.ongoingCall.observe(viewLifecycleOwner) {
            if (it is CallState.InCall && it.publicKey.string() == contactPubKey) {
                ongoingCall.container.visibility = View.VISIBLE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ongoingCall.duration.visibility = View.VISIBLE
                    ongoingCall.duration.base = it.startTime
                    ongoingCall.duration.isCountDown = false
                    ongoingCall.duration.start()
                } else {
                    ongoingCall.duration.visibility = View.GONE
                }
            } else {
                ongoingCall.container.visibility = View.GONE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ongoingCall.duration.stop()
                }
            }
        }

        ongoingCall.endCall.setOnClickListener { viewModel.onEndCall() }
        ongoingCall.info.setOnClickListener { navigateToCallScreen() }

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
                        WindowInsetsControllerCompat(requireActivity().window, view).hide(WindowInsetsCompat.Type.ime())
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
            WindowInsetsControllerCompat(requireActivity().window, view).hide(WindowInsetsCompat.Type.ime())
            attachFilesLauncher.launch(arrayOf("*/*"))
        }

        outgoingMessage.doAfterTextChanged {
            viewModel.setTyping(outgoingMessage.text.isNotEmpty())
            updateActions()
        }

        updateActions()

        if (arguments?.getBoolean(FOCUS_ON_MESSAGE_BOX) == true) {
            outgoingMessage.requestFocus()
        }
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
                    .setTitle(R.string.delete_message)
                    .setMessage(
                        getString(
                            R.string.delete_message_confirm,
                            message.message.truncated(MAX_CONFIRM_DELETE_STRING_LENGTH)
                        )
                    )
                    .setPositiveButton(R.string.delete) { _, _ ->
                        viewModel.delete(message)
                    }
                    .setNegativeButton(android.R.string.cancel, null).show()
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
                exportFtLauncher.launch(message.message)
                true
            }
            else -> super.onContextItemSelected(item)
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
            ContextCompat.getColor(
                requireContext(),
                if (attach.isEnabled) R.color.colorPrimary else android.R.color.darker_gray,
            )
        )
    }

    private fun navigateToCallScreen() {
        view?.let { WindowInsetsControllerCompat(requireActivity().window, it).hide(WindowInsetsCompat.Type.ime()) }
        findNavController().navigate(
            R.id.action_chatFragment_to_callFragment,
            bundleOf(CONTACT_PUBLIC_KEY to contactPubKey)
        )
    }
}
