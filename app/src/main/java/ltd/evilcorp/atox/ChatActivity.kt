package ltd.evilcorp.atox

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val username = intent.getStringExtra("username")
        Log.e("ChatActivity", username)

        val listView = findViewById<ListView>(R.id.messages)
        val messages = ArrayList<MessageModel>()
        messages.add(MessageModel("hello", Sender.Received))
        messages.add(MessageModel("how are you", Sender.Received))
        messages.add(MessageModel("I'm good, thanks.", Sender.Sent))
        listView.adapter = MessagesAdapter(this, messages)
    }
}
