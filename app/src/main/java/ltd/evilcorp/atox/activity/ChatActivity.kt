package ltd.evilcorp.atox.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.contact_list_view_item.*
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

            val sendMsg = Message()
            sendMsg.what = ToxThread.msgSendMsg
            sendMsg.obj = outgoingMessage.text.toString()
            sendMsg.arg1 = friendNumber

            App.toxThread.handler.sendMessage(sendMsg)

            messages.add(MessageModel(outgoingMessage.text.toString(), Sender.Sent))
            adapter.notifyDataSetChanged()
            outgoingMessage.text.clear()
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = intent.getStringExtra("username")
    }
}
