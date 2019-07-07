package ltd.evilcorp.atox.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ltd.evilcorp.atox.ui.contactlist.ContactListFragment


class ContactListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    android.R.id.content,
                    ContactListFragment.newInstance()
                )
                .commitNow()
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(android.R.id.content) as ContactListFragment
        currentFragment.onBackPressed()
    }
}
