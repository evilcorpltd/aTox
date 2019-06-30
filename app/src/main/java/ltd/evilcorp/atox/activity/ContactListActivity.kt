package ltd.evilcorp.atox.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_contact_list.*
import kotlinx.android.synthetic.main.contact_list_view_item.view.*
import kotlinx.android.synthetic.main.nav_header_contact_list.view.*
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.repository.ContactRepository
import ltd.evilcorp.atox.tox.ToxThread.Companion.msgDeleteContact
import ltd.evilcorp.atox.tox.byteArrayToHex
import ltd.evilcorp.atox.tox.hexToByteArray
import ltd.evilcorp.atox.ui.ContactAdapter
import ltd.evilcorp.atox.vo.Contact
import javax.inject.Inject

class ContactListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    @Inject
    lateinit var contactRepository: ContactRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)
        setSupportActionBar(toolbar)

        navView.getHeaderView(0).profileName.text = App.profile

        contactList.adapter = ContactAdapter(this, this, contactRepository)
        registerForContextMenu(contactList)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val info = menuInfo as AdapterContextMenuInfo
        menu.setHeaderTitle(info.targetView.name.text)

        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.contact_list_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                val info = item.menuInfo as AdapterContextMenuInfo
                val contact = contactList.adapter.getItem(info.position) as Contact

                with(App.toxThread.handler) {
                    sendMessage(obtainMessage(msgDeleteContact, contact.publicKey.byteArrayToHex()))
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
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
            R.id.copy_tox_id -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.primaryClip = ClipData.newPlainText(getText(R.string.tox_id), App.toxThread.toxId)

                Toast.makeText(this, getText(R.string.tox_id_copied), Toast.LENGTH_SHORT).show()
            }
            R.id.add_contact -> {
                startActivity(Intent(this, AddContactActivity::class.java))
            }
            R.id.settings -> {
                // TODO(robinlinden): Settings activity
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
