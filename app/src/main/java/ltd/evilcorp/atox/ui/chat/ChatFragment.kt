package ltd.evilcorp.atox.ui.chat

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.android.synthetic.main.chat_fragment.view.*
import kotlinx.android.synthetic.main.profile_image_layout.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.di.ViewModelFactory
import ltd.evilcorp.atox.ui.MessagesAdapter
import ltd.evilcorp.atox.ui.colorByStatus
import ltd.evilcorp.atox.vo.ConnectionStatus
import javax.inject.Inject

class ChatFragment : Fragment() {
    companion object {
        fun newInstance(publicKey: ByteArray): Fragment {
            val fragment = ChatFragment()
            val arguments = Bundle()
            arguments.putByteArray("publicKey", publicKey)
            fragment.arguments = arguments

            return fragment
        }
    }

    @Inject
    lateinit var vmFactory: ViewModelFactory
    private lateinit var viewModel: ChatViewModel

    private var contactOnline = false

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.chat_fragment, container, false)

        viewModel = ViewModelProviders.of(this, vmFactory).get(ChatViewModel::class.java)
        viewModel.publicKey = arguments!!.getByteArray("publicKey")!!

        activity!!.apply {
            setActionBar(layout.toolbar)
            actionBar!!.apply {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
            }

            layout.toolbar.setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }

        viewModel.contact.observe(this, Observer {
            layout.title.text = it.name
            layout.statusIndicator.setColorFilter(colorByStatus(resources, it))
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
