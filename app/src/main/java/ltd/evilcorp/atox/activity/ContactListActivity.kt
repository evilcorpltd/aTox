package ltd.evilcorp.atox.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import com.google.android.material.navigation.NavigationView
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_contact_list.*
import kotlinx.android.synthetic.main.contact_list_view_item.view.*
import kotlinx.android.synthetic.main.nav_header_contact_list.view.*
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.repository.ContactRepository
import ltd.evilcorp.atox.tox.hexToByteArray
import ltd.evilcorp.atox.ui.ContactAdapter
import ltd.evilcorp.atox.vo.ConnectionStatus
import ltd.evilcorp.atox.vo.Contact
import ltd.evilcorp.atox.vo.UserStatus
import javax.inject.Inject
import kotlin.random.Random

class ContactListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    @Inject
    lateinit var contactRepository: ContactRepository

    private var contacts: List<Contact> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)
        setSupportActionBar(toolbar)

        navView.getHeaderView(0).profileName.text = App.profile

        contactRepository.getContacts().observe(this, Observer {
            contacts = it
        })

        contactList.adapter = ContactAdapter(this, this, contactRepository)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_contact -> {
                startActivity(Intent(this, AddContactActivity::class.java))
            }
            R.id.add_echobot -> {
                val pubKey =
                    ("${Random.nextInt(9)}" +
                        "${Random.nextInt(9)}" +
                        "${Random.nextInt(9)}" +
                        "${Random.nextInt(9)}" +
                        "8406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6").hexToByteArray()
                val contact = Contact(
                    pubKey,
                    -1,
                    "new EchoBot ${Random.nextInt(-1000, 1000)}",
                    Random.nextBytes(12).toString(),
                    "Never",
                    UserStatus.values().random(),
                    ConnectionStatus.values().random(),
                    Random.nextInt() % 2 == 0
                )
                contactRepository.addContact(contact)
            }
            R.id.settings -> {
                // TODO(robinlinden): Settings activity
            }
            R.id.destroy_everything -> {
                finishAffinity()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun openChat(view: View) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("publicKey", view.publicKey.text.toString().hexToByteArray())
        startActivity(intent)
    }
}
