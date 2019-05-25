package ltd.evilcorp.atox.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_chat.*
import ltd.evilcorp.atox.*
import ltd.evilcorp.atox.tox.ToxThread
import ltd.evilcorp.atox.ui.MessagesAdapter
import ltd.evilcorp.atox.vo.MessageModel
import ltd.evilcorp.atox.vo.Sender

class ChatActivity : AppCompatActivity() {
    private val messagesModel = ArrayList<MessageModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val adapter = MessagesAdapter(this, messagesModel)
        messages.adapter = adapter

        send.setOnClickListener {
            val friendNumber: Int = intent.getIntExtra("friendNumber", 0)

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
                send.isEnabled = s!!.toString().isNotEmpty()
            }
        })

        toolbar.title = intent.getStringExtra("username")
    }
}
