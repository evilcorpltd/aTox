package ltd.evilcorp.atox.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_contact.*
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.MsgAddContact
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.ToxThread

class AddContactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        toxId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val content = s?.toString()
                // TODO(robinlinden): Checksum error check.
                toxId.error = if (content?.length == 76) null else getString(R.string.tox_id_error_length)
                add.isEnabled = toxId.error == null
            }
        })

        message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val content = s?.toString()
                message.error = if (content?.length != 0) null else getString(R.string.add_contact_message_error_empty)
                add.isEnabled = message.error == null
            }
        })

        add.setOnClickListener {
            with(App.toxThread.handler) {
                val addContactMessage = obtainMessage(
                    ToxThread.msgAddContact,
                    MsgAddContact(toxId.text.toString(), message.text.toString())
                )
                sendMessage(addContactMessage)
            }
            finish()
        }

        add.isEnabled = false
    }
}
