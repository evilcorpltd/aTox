package ltd.evilcorp.atox.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.contact_list_view_item.view.*
import ltd.evilcorp.atox.ContactAdapter
import ltd.evilcorp.atox.ContactModel
import ltd.evilcorp.atox.R
import kotlin.random.Random

class ContactListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        val listView = findViewById<ListView>(R.id.contactList)
        val contacts = ArrayList<ContactModel>()
        contacts.add(
            ContactModel(
                "EchoBot",
                "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6",
                "18:30"
            )
        )
        contacts.add(
            ContactModel(
                "Also EchoBot",
                "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6",
                "Dec 31"
            )
        )
        contacts.add(
            ContactModel(
                "EchoBot 3: Reckoning",
                "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6",
                "23.09.17"
            )
        )
        val adapter = ContactAdapter(this, contacts)
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
                    contacts.add(
                        ContactModel(
                            "new EchoBot ${Random.nextInt(-1000, 1000)}",
                            "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6",
                            "Never"
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
        startActivity(intent)
    }
}
