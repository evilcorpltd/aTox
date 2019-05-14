package ltd.evilcorp.atox

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ListView
import kotlinx.android.synthetic.main.contact_list_view_item.view.*

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
        listView.adapter = ContactAdapter(this, contacts)

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

        toolbar.title = resources.getString(R.string.app_name)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    fun openChat(view: View) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("username", view.name.text)
        startActivity(intent)
    }
}
