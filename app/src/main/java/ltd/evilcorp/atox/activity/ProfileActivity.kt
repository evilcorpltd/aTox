package ltd.evilcorp.atox.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import im.tox.tox4j.core.options.SaveDataOptions
import kotlinx.android.synthetic.main.activity_profile.*
import ltd.evilcorp.atox.*
import java.io.File

const val MSG_CONTACTS_LOADED: Int = 0

typealias ToxContactInfo = Pair<ByteArray, Int> // PublicKey -> FriendNumber

@Suppress("UNCHECKED_CAST")
class UIMessageHandler : Handler(Looper.getMainLooper()) {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_CONTACTS_LOADED -> {
                val contacts = msg.obj as List<ToxContactInfo>
                for ((publicKey, friendNumber) in contacts) {
                    App.contacts.add(
                        ContactModel(
                            "Unknown",
                            publicKey.byteArrayToHex().toUpperCase(),
                            "Never",
                            friendNumber
                        )
                    )
                }
            }
        }
    }
}

private fun loadToxSave(saveFile: File): ByteArray? {
    if (!saveFile.exists()) {
        return null
    }

    return saveFile.readBytes()
}

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var profile: File? = null
        filesDir.walk().forEach {
            if (it.extension == "tox" && it.isFile) {
                profile = it
                App.profile = it.nameWithoutExtension
            }
        }

        if (profile != null) {
            val data = loadToxSave(profile!!)
            if (data != null) {
                App.toxThread = ToxThread(filesDir.toString(), SaveDataOptions.ToxSave(data), UIMessageHandler())
                startActivity(Intent(this, ContactListActivity::class.java))
                finish()
            }
        }

        setContentView(R.layout.activity_profile)
        btnCreate.setOnClickListener {
            btnCreate.isEnabled = false
            App.profile = if (username.text.isNotEmpty()) username.text.toString() else "aTox user"
            App.password = if (password.text.isNotEmpty()) password.text.toString() else ""
            startActivity(Intent(this@ProfileActivity, ContactListActivity::class.java))
            App.toxThread = ToxThread(filesDir.toString(), SaveDataOptions.`None$`(), UIMessageHandler())

            with(App.toxThread.handler) {
                sendMessage(obtainMessage(ToxThread.msgSetName, App.profile))
            }

            finish()
        }
    }
}
