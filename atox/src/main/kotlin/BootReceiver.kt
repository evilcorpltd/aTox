package ltd.evilcorp.atox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import javax.inject.Inject
import ltd.evilcorp.atox.tox.ToxStarter

class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var toxStarter: ToxStarter

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            (context.applicationContext as App).component.inject(this)
            toxStarter.tryLoadTox(null)
        }
    }
}
