package ltd.evilcorp.atox.activity

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.activity_chat.*
import ltd.evilcorp.atox.*

class ChatActivity : AppCompatActivity() {
    private val messages = ArrayList<MessageModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val adapter = MessagesAdapter(this, messages)
        val messageView = findViewById<ListView>(R.id.messages)
        messageView.adapter = adapter

        send.setOnClickListener {
            val friendNumber: Int = intent.getIntExtra("friendNumber", 0)

            with(App.toxThread.handler) {
                sendMessage(obtainMessage(ToxThread.msgSendMsg, friendNumber, 0, outgoingMessage.text.toString()))
            }

            messages.add(MessageModel(outgoingMessage.text.toString(), Sender.Sent))
            adapter.notifyDataSetChanged()
            outgoingMessage.text.clear()
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = intent.getStringExtra("username")
    }
}
