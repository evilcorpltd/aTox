package ltd.evilcorp.atox.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ltd.evilcorp.atox.ui.addcontact.AddContactFragment

class AddContactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, AddContactFragment.newInstance())
                .commitNow()
        }
    }
}
