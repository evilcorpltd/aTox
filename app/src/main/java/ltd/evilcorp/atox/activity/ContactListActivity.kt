package ltd.evilcorp.atox.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.android.synthetic.main.contact_list_view_item.view.*
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.ContactAdapter
import ltd.evilcorp.atox.ContactModel
import ltd.evilcorp.atox.R
import kotlin.random.Random

class ContactListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        val listView = findViewById<ListView>(R.id.contactList)
        App.contacts.add(
            ContactModel(
                "EchoBot",
                "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6",
                "18:30",
                0
            )
        )
        App.contacts.add(
            ContactModel(
                "Also EchoBot",
                "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6",
                "Dec 31",
                0
            )
        )
        App.contacts.add(
            ContactModel(
                "EchoBot 3: Reckoning",
                "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6",
                "23.09.17",
                0
            )
        )

        val adapter = ContactAdapter(this, App.contacts)
        listView.adapter = adapter

        val drawer = findViewById<DrawerLayout>(R.id.drawerLayout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val drawerToggle = object : ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {}

        drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val menu = findViewById<ListView>(R.id.menuDrawer)
        menu.setOnItemClickListener { _: AdapterView<*>, _: View, _: Int, id: Long ->
            when (id) {
                0L -> {
                    App.contacts.add(
                        ContactModel(
                            "new EchoBot ${Random.nextInt(-1000, 1000)}",
                            "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6",
                            "Never",
                            0
                        )
                    )
                    adapter.notifyDataSetChanged()
                }
                1L -> startActivity(Intent(this, AddContactActivity::class.java))
                2L -> finishAffinity()
            }

            val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        val menuItems = ArrayList<String>()
        menuItems.add("Add EchoBot")
        menuItems.add("Add contact")
        menuItems.add("Destroy everything")

        menu.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, menuItems)

        toolbar.title = resources.getString(R.string.app_name)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
    }

    override fun onBackPressed() {
        val drawer: DrawerLayout = findViewById(R.id.drawerLayout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            moveTaskToBack(true)
        }
    }

    fun openChat(view: View) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("username", view.name.text)

        //TODO(endoffile78) figure out a better way to get the friend number
        var friendNumber = 0
        App.contacts.forEach() lit@{
            if (it.name == view.name.text) {
                friendNumber = it.friendNumber
                return@lit // break
            }
        }

        intent.putExtra("friendNumber", friendNumber)
        startActivity(intent)
    }
}
