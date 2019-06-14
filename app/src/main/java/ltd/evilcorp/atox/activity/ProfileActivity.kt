package ltd.evilcorp.atox.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import im.tox.tox4j.core.options.SaveDataOptions
import kotlinx.android.synthetic.main.activity_profile.*
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.tox.ToxThread
import ltd.evilcorp.atox.tox.ToxThreadFactory
import java.io.File
import javax.inject.Inject

private fun loadToxSave(saveFile: File): ByteArray? {
    if (!saveFile.exists()) {
        return null
    }

    return saveFile.readBytes()
}

class ProfileActivity : AppCompatActivity() {
    @Inject
    lateinit var toxThreadFactory: ToxThreadFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        var profile: File? = null
        filesDir.walk().forEach {
            if (it.extension == "tox" && it.isFile) {
                profile = it
            }
        }

        if (profile != null) {
            val data = loadToxSave(profile!!)
            if (data != null) {
                App.toxThread = toxThreadFactory.create(filesDir.toString(), SaveDataOptions.ToxSave(data))
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
            App.toxThread = toxThreadFactory.create(filesDir.toString(), SaveDataOptions.`None$`())

            with(App.toxThread.handler) {
                sendMessage(obtainMessage(ToxThread.msgSetName, App.profile))
            }

            finish()
        }
    }
}
