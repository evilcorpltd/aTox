package ltd.evilcorp.atox

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import javax.inject.Inject
import javax.inject.Singleton

private const val PROXIMITY_SCREEN_OFF_WAKELOCK_TAG = "atox:ProximityScreenOff"

@Singleton
class ProximityScreenOff @Inject constructor(context: Context) {
    private val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var screenOffWakeLock: PowerManager.WakeLock? = null

    @SuppressLint("WakelockTimeout")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun acquire() {
        if (screenOffWakeLock == null) {
            screenOffWakeLock =
                powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, PROXIMITY_SCREEN_OFF_WAKELOCK_TAG)
            screenOffWakeLock?.acquire()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun release() {
        screenOffWakeLock?.apply {
            if (isHeld) release()
            screenOffWakeLock = null
        }
    }
}
