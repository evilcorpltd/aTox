package ltd.evilcorp.atox.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ltd.evilcorp.atox.ui.chat.ChatFragment

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    android.R.id.content,
                    ChatFragment.newInstance(intent.getStringExtra("publicKey"))
                )
                .commitNow()
        }
    }
}
