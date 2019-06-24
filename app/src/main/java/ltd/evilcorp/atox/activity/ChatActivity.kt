package ltd.evilcorp.atox.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import ltd.evilcorp.atox.di.ViewModelFactory
import ltd.evilcorp.atox.ui.ChatFragment
import javax.inject.Inject

class ChatActivity : AppCompatActivity() {
    @Inject
    lateinit var vmFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    android.R.id.content,
                    ChatFragment.newInstance(intent.getByteArrayExtra("publicKey"), vmFactory)
                )
                .commitNow()
        }
    }
}
