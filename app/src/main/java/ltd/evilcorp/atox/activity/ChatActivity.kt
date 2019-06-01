package ltd.evilcorp.atox.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.repository.ContactRepository
import ltd.evilcorp.atox.repository.MessageRepository
import ltd.evilcorp.atox.ui.ChatFragment
import javax.inject.Inject

class ChatActivity : AppCompatActivity() {
    @Inject
    lateinit var contactRepository: ContactRepository

    @Inject
    lateinit var messageRepository: MessageRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    ChatFragment.newInstance(
                        intent.getByteArrayExtra("publicKey"),
                        contactRepository,
                        messageRepository
                    )
                )
                .commitNow()
        }
    }
}
