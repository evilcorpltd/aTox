package ltd.evilcorp.atox.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.android.synthetic.main.chat_fragment.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.repository.ContactRepository
import ltd.evilcorp.atox.repository.MessageRepository
import ltd.evilcorp.atox.vo.ConnectionStatus

class ChatFragment(
    private val publicKey: ByteArray,
    private val contactRepository: ContactRepository,
    private val messageRepository: MessageRepository
) : Fragment() {
    companion object {
        fun newInstance(
            publicKey: ByteArray,
            contactRepository: ContactRepository,
            messageRepository: MessageRepository
        ): Fragment = ChatFragment(publicKey, contactRepository, messageRepository)
    }

    private lateinit var viewModel: ChatViewModel

    private var contactOnline = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.chat_fragment, container, false)

        viewModel = ChatViewModel(publicKey, contactRepository, messageRepository)
        viewModel.contact.observe(this, Observer {
            layout.toolbar.title = it.name
            contactOnline = it.connectionStatus != ConnectionStatus.NONE
            updateSendButton(layout)
        })

        val adapter = MessagesAdapter(inflater)
        layout.messages.adapter = adapter

        viewModel.messages.observe(this, Observer {
            adapter.messages = it
            adapter.notifyDataSetChanged()
        })

        layout.send.setOnClickListener {
            val message = layout.outgoingMessage.text.toString()
            layout.outgoingMessage.text.clear()
            viewModel.sendMessage(message)
        }

        layout.send.isEnabled = false

        layout.outgoingMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSendButton(layout)
            }
        })

        return layout
    }

    private fun updateSendButton(layout: View) {
        layout.send.isEnabled = outgoingMessage.text.isNotEmpty() && contactOnline
    }
}
