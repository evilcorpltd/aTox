package ltd.evilcorp.atox.ui.chat

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.chat_fragment.view.*
import kotlinx.android.synthetic.main.profile_image_layout.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.activity.CONTACT_PUBLIC_KEY
import ltd.evilcorp.atox.di.ViewModelFactory
import ltd.evilcorp.atox.ui.MessagesAdapter
import ltd.evilcorp.atox.ui.colorByStatus
import ltd.evilcorp.atox.vo.ConnectionStatus
import javax.inject.Inject

class ChatFragment : Fragment() {
    companion object {
        fun newInstance(publicKey: String) = ChatFragment().apply {
            arguments = Bundle().apply {
                putString(CONTACT_PUBLIC_KEY, publicKey)
            }
        }
    }

    @Inject
    lateinit var vmFactory: ViewModelFactory
    private val viewModel: ChatViewModel by lazy {
        ViewModelProviders.of(this, vmFactory).get(ChatViewModel::class.java).apply {
            publicKey = arguments!!.getString(CONTACT_PUBLIC_KEY)!!
        }
    }

    private var contactName = ""
    private var contactOnline = false

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.chat_fragment, container, false).apply {
        requireActivity().let {
            it.setActionBar(toolbar)
            it.actionBar!!.apply {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
            }
        }

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

        viewModel.contact.observe(this@ChatFragment, Observer {
            contactName = it.name
            contactOnline = it.connectionStatus != ConnectionStatus.NONE

            title.text = contactName
            statusIndicator.setColorFilter(colorByStatus(resources, it))
            updateSendButton(this)
        })

        val adapter = MessagesAdapter(inflater)
        messages.adapter = adapter

        viewModel.messages.observe(this@ChatFragment, Observer {
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

    private fun updateSendButton(layout: View) {
        layout.send.isEnabled = layout.outgoingMessage.text.isNotEmpty() && contactOnline
    }
}
