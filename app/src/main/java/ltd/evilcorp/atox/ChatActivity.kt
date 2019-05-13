package ltd.evilcorp.atox

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {
    private val messages = ArrayList<MessageModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val username = intent.getStringExtra("username")
        Log.e("ChatActivity", username)

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
    }
}
