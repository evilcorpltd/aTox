package ltd.evilcorp.atox.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_chat.*
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.repository.ContactRepository
import ltd.evilcorp.atox.tox.ToxThread
import ltd.evilcorp.atox.ui.MessagesAdapter
import ltd.evilcorp.atox.vo.ConnectionStatus
import ltd.evilcorp.atox.vo.MessageModel
import ltd.evilcorp.atox.vo.Sender
import javax.inject.Inject

class ChatActivity : AppCompatActivity() {
    private val messagesModel = ArrayList<MessageModel>()

    @Inject
    lateinit var contactRepository: ContactRepository

    private var contactOnline = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val adapter = MessagesAdapter(this, messagesModel)
        messages.adapter = adapter

        val friendNumber: Int = intent.getIntExtra("friendNumber", 0)
        val contact = contactRepository.getContact(friendNumber)
        contact.observe(this, Observer {
            contactOnline = it.connectionStatus != ConnectionStatus.NONE
            updateSendButton()
        })

        send.setOnClickListener {
            with(App.toxThread.handler) {
                sendMessage(obtainMessage(ToxThread.msgSendMsg, friendNumber, 0, outgoingMessage.text.toString()))
            }

            messagesModel.add(
                MessageModel(
                    outgoingMessage.text.toString(),
                    Sender.Sent
                )
            )
            adapter.notifyDataSetChanged()
            outgoingMessage.text.clear()
        }
        send.isEnabled = false

        outgoingMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSendButton()
            }
        })

        updateSendButton()

        toolbar.title = intent.getStringExtra("username")
    }

    private fun updateSendButton() {
        send.isEnabled = outgoingMessage.text.isNotEmpty() && contactOnline
    }
}
