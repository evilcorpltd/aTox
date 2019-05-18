package ltd.evilcorp.atox.activity

import android.content.Intent
import android.os.Bundle
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import im.tox.tox4j.core.options.SaveDataOptions
import kotlinx.android.synthetic.main.activity_profile.*
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.ToxThread
import java.io.File

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
                App.toxThread = ToxThread(filesDir.toString(), SaveDataOptions.ToxSave(data))
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
            App.toxThread = ToxThread(filesDir.toString(), SaveDataOptions.`None$`())

            val nameChangeMsg = Message()
            nameChangeMsg.what = ToxThread.msgSetName
            nameChangeMsg.obj = App.profile
            App.toxThread.handler.sendMessage(nameChangeMsg)
            App.toxThread.handler.sendEmptyMessage(ToxThread.msgSave)

            finish()
        }
    }
}
