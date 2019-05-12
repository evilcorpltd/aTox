package ltd.evilcorp.atox

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        btnCreate.setOnClickListener {
            btnCreate.isEnabled = false
            App.profile = if (username.text.isNotEmpty()) username.text.toString() else "aTox user"
            App.password = if (password.text.isNotEmpty()) password.text.toString() else ""
            startActivity(Intent(this@ProfileActivity, ContactListActivity::class.java))
            finish()
        }
    }

}
