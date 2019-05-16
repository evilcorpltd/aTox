package ltd.evilcorp.atox.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

        App.toxThread = ToxThread(filesDir.toString())

        var profile: File? = null
        filesDir.walk().forEach {
            if (it.extension.equals("tox") && it.isFile) {
                profile = it
                App.profile = it.nameWithoutExtension
                Log.e("Profile", "Found profile: ${profile.toString()}")
            }
        }

        if (profile != null) {
            var saveOption: SaveDataOptions = SaveDataOptions.`None$`()
            val data = loadToxSave(profile!!)
            if (data != null) {
                saveOption = SaveDataOptions.`ToxSave`(data)
                App.toxThread.start(saveOption)
                Log.e("Profile", "Skipping create profile")
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

            App.toxThread.start(SaveDataOptions.`None$`())
            App.toxThread.triggerSave()

            finish()
        }
    }
}
