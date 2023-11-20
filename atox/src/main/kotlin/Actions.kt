package ltd.evilcorp.atox

object Actions {
    const val PREFIX = "${BuildConfig.APPLICATION_ID}.intent.action"
    const val ACTION_CONNECT = "$PREFIX.CONNECT"
    const val ACTION_DISCONNECT = "$PREFIX.DISCONNECT"
    const val ACTION_SYSTEM_CONNECT = "android.net.VpnService"
    const val EVENT_CONNECTED = "$PREFIX.CONNECTED"
    const val EVENT_DISCONNECTED = "$PREFIX.DISCONNECTED"
    const val EVENT_ABORTED = "$PREFIX.ABORTED"
    const val TOX_SCHEME = "tox"

}
