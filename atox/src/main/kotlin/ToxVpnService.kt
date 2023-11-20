package ltd.evilcorp.atox

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import ltd.evilcorp.atox.tox.EventListenerCallbacks
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.Tox
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject


private const val TAG = "ToxVpnService"

class ToxVpnService : VpnService() {
    @Inject
    lateinit var tox: Tox

    @Inject
    lateinit var toxStarter: ToxStarter

    @Inject
    lateinit var contactManager: ContactManager

    @Inject
    lateinit var eventListenerCallbacks: EventListenerCallbacks

    private var mHandler: Handler? = null
    private val mVpnThread = AtomicReference<Thread>()
    private var mConfigureIntent: PendingIntent? = null

    override fun onDestroy() {
        stopVpn()
        stopForeground(true)
        super.onDestroy()
    }

    override fun onCreate() {
        (application as App).component.inject(this)
        super.onCreate()
        // The handler is only used to show messages.
        if (mHandler == null) {
            mHandler = Handler { msg ->
                Toast.makeText(this@ToxVpnService, msg.what, Toast.LENGTH_SHORT).show()
                if (msg.what != R.string.ending) {
                    // Become a foreground service. Background services can be VPN services too, but they can
                    // be killed by background check before getting a chance to receive onRevoke().
                    updateForegroundNotification(msg.what)
                } else {
                    stopForeground(true)
                }
                true
            }
        }

        // Create the intent to "configure" the connection
        mConfigureIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "Intent received: ${intent.toString()}")

        when {
            intent.action == Actions.ACTION_CONNECT /*&& intent.scheme == Actions.TOX_SCHEME*/ ->
                startVpn()

            intent.action == Actions.ACTION_DISCONNECT ->
                stopVpn()

            intent.action == Actions.ACTION_SYSTEM_CONNECT ->
                restorePreviousConnection()

            else ->
                throw IllegalArgumentException("Invalid intent action received.")
        }

        return Service.START_NOT_STICKY
    }

    private fun restorePreviousConnection() {
        Log.i(TAG, "Restoring previous connection.")
        startVpn()
    }

    private fun startVpn(): Unit = synchronized(this) {
        Log.i(TAG, "Starting vpn.")

        val mtu: Int = resources.getInteger(R.integer.mtu)
        //Log.d(TAG, "mtu: $mtu")

        try {
            val statusMessage = tox.getStatusMessage()
            Log.d(TAG, "Status message: $statusMessage")
            val regex = """\{\"ownip\":\"(?<ip>.+?)\"\}""".toRegex()
            val ownip = regex.matchEntire(statusMessage)!!.groups["ip"]!!.value

            val ownInetAddress = InetAddress.getByName(ownip)
            val netAddress = computeNetworkAddress(ownInetAddress, InetAddress.getByName("255.255.255.0"))!!
            val builder = Builder()
            builder.setSession("tox")
                .addAddress(ownip, 32)
                .setMtu(mtu)
                .also { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) it.setMetered(false) }

//                    builder.addRoute(netAddress, 24)
            val routes = mutableMapOf<InetAddress, Contact>()

            runBlocking {

                contactManager.getAll()
                    .firstOrNull { it.size > 0 }?.map { contact ->
                            val matches = regex.matchEntire(contact.statusMessage)
                            if (matches != null) {
                                val ip = matches?.groups?.get("ip")?.value
                                if (ip != null) {
                                    Log.d(TAG, "Contact ip: $ip")
                                    builder.addRoute(ip, 32)
                                    routes[InetAddress.getByName(ip)] = contact
                                }
                            }
                        }
            }
            val iface = builder.establish()!!
            startToxVpnRunnable(ToxVpnRunnable(eventListenerCallbacks, tox, this, iface, contactManager, routes))
        } catch (e: NullPointerException) {
            Log.e(TAG, "Wrong status for ToxVpn", e)
        }
    }

    private fun startToxVpnRunnable(runnable: ToxVpnRunnable) {
        // Replace any existing connecting thread with the  new one.
//        running.getAndSet(true)
        val thread: Thread = Thread(runnable, "ToxVpnThread")
        storeConnectingThread(thread)

        // Handler to mark as connected once onEstablish is called.
        mConfigureIntent?.let { runnable.setConfigureIntent(it) }
        runnable.setOnConnectListener(
            object : ToxVpnRunnable.OnConnectListener {
                override fun onConnectStage(stage: ToxVpnRunnable.OnConnectListener.Stage?) {
                    when (stage) {
                        ToxVpnRunnable.OnConnectListener.Stage.taskLaunch -> mHandler?.sendEmptyMessage(R.string.launching)
                        ToxVpnRunnable.OnConnectListener.Stage.connecting -> mHandler?.sendEmptyMessage(R.string.connecting)
                        ToxVpnRunnable.OnConnectListener.Stage.establish -> mHandler?.sendEmptyMessage(R.string.connected)
                        ToxVpnRunnable.OnConnectListener.Stage.disconnected -> mHandler?.sendEmptyMessage(R.string.disconnected)
                        ToxVpnRunnable.OnConnectListener.Stage.taskTerminate -> mHandler?.sendEmptyMessage(R.string.ending)
                        else -> {}
                    }
                }
            },
        )
        thread.start()
    }

    private fun storeConnectingThread(thread: Thread?) {
        val oldThread: Thread? = mVpnThread.getAndSet(thread)
        if (oldThread != null) {
            oldThread.interrupt()
        }
    }
    private fun stopVpn() {
        Log.i(TAG, "Stopping any running tox daemon.")
        if (mVpnThread.get() != null) {
            storeConnectingThread(null)
            broadcastEvent(Actions.EVENT_DISCONNECTED)
        }
    }

    private fun updateForegroundNotification(message: Int) {
        val CHANNEL_ID = "ToxVpn"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel: NotificationChannel
            channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)
            val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            mgr.createNotificationChannel(channel)
            startForeground(
                1,
                Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_vpn)
                    .setContentText(getString(message))
                    .setContentIntent(mConfigureIntent)
                    .build(),
            )
        } else {
            startForeground(
                1,
                Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_vpn)
                    .setContentText(getString(message))
                    .setContentIntent(mConfigureIntent)
                    .setAutoCancel(false)
                    .setTicker("Foreground Service Start")
                    .setContentTitle(getString(R.string.app_name))
                    .build(),
            )
        }
    }

    private fun broadcastEvent(event: String) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(event))
    }

    /**
     * Compute network address from IP and mask
     * @param ip
     * @param mask
     * @return
     */
    fun computeNetworkAddress(
        ip: InetAddress?,
        mask: InetAddress?,
    ): String? {
        val addr = InetAddressToInt(ip)
        val maskInt = InetAddressToInt(mask)
        return IpToString(addr and maskInt)
    }

    fun computeNetworkAddress(
        ip: InetAddress?,
        maskInt: Int,
    ): String? {
        val addr = InetAddressToInt(ip)
        return IpToString(addr and maskInt)
    }

    /** Convert ip to an integer
     * @param ip InetAddress to convert
     * @return Integer
     */
    fun InetAddressToInt(ip: InetAddress?): Int {
        if (ip == null) return -1
        val adr = ip.address
        val i = IntArray(4)
        for (j in 0..3) {
            i[j] = (if (adr[j] < 0) 256 + adr[j] else adr[j]).toInt()
        }
        return i[3] + (i[2] shl 8) + (i[1] shl 16) + (i[0] shl 24)
    }

    /**
     * Convert an IP address stored in an int to its string representation.
     * @param address
     * @return
     */
    fun IpToString(address: Int): String? {
        var address = address
        val sa = StringBuffer()
        for (i in 0..3) {
            sa.append(0xff and (address shr 24))
            address = address shl 8
            if (i != 4 - 1) sa.append('.')
        }
        return sa.toString()
    }
}
