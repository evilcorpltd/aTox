package ltd.evilcorp.atox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import dagger.android.AndroidInjection
import ltd.evilcorp.atox.di.ViewModelFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var vmFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(
            PreferenceManager.getDefaultSharedPreferences(applicationContext).getInt("theme", 0)
        )

        setContentView(R.layout.activity_main)
    }
}
