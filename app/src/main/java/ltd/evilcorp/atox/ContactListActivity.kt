package ltd.evilcorp.atox

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView

class ContactListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        val listView = findViewById<ListView>(R.id.contactList)
        val contacts = ArrayList<ContactModel>()
        contacts.add(
            ContactModel(
                "EchoBot",
                "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6"
            )
        )
        contacts.add(
            ContactModel(
                "Also EchoBot",
                "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39218F515C39A6"
            )
        )
        listView.adapter = ContactAdapter(this, contacts)
    }
}
