package ltd.evilcorp.atox.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.contact_list_view_item.view.*
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.ContactAdapter
import ltd.evilcorp.atox.ContactModel
import ltd.evilcorp.atox.R
import kotlin.random.Random

class ContactListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val contactAdapter by lazy { ContactAdapter(this, App.contacts) }
    private val navigationHeader by lazy { findViewById<NavigationView>(R.id.navView).getHeaderView(0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val name = navigationHeader.findViewById<TextView>(R.id.profileName)
        name.text = App.profile

        val contactList = findViewById<ListView>(R.id.contactList)
        contactList.adapter = contactAdapter

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.navView)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        val drawer: DrawerLayout = findViewById(R.id.drawerLayout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
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
                App.contacts.add(
                    ContactModel(
                        "new EchoBot ${Random.nextInt(-1000, 1000)}",
                        "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6",
                        "Never",
                        0
                    )
                )
                contactAdapter.notifyDataSetChanged()
            }
            R.id.settings -> {
                // TODO(robinlinden): Settings activity
            }
            R.id.destroy_everything -> {
                finishAffinity()
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun openChat(view: View) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("username", view.name.text)

        //TODO(endoffile78) figure out a better way to get the friend number
        var friendNumber = 0
        App.contacts.forEach lit@{
            if (it.name == view.name.text) {
                friendNumber = it.friendNumber
                return@lit // break
            }
        }

        intent.putExtra("friendNumber", friendNumber)
        startActivity(intent)
    }
}
