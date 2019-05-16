package ltd.evilcorp.atox.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_chat.*
import ltd.evilcorp.atox.MessageModel
import ltd.evilcorp.atox.MessagesAdapter
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.Sender

class ChatActivity : AppCompatActivity() {
    private val messages = ArrayList<MessageModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        messages.add(MessageModel("hello", Sender.Received))
        messages.add(MessageModel("how are you", Sender.Received))
        messages.add(MessageModel("I'm good, thanks.", Sender.Sent))

        val adapter = MessagesAdapter(this, messages)
        val messageView = findViewById<ListView>(R.id.messages)
        messageView.adapter = adapter

        send.setOnClickListener {
            messages.add(MessageModel(outgoingMessage.text.toString(), Sender.Sent))
            adapter.notifyDataSetChanged()
            outgoingMessage.text.clear()
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = intent.getStringExtra("username")
    }
}
