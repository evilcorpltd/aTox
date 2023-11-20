package ltd.evilcorp.atox

import android.app.PendingIntent
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.flow.firstOrNull
import ltd.evilcorp.atox.tox.EventListenerCallbacks
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox
import org.pcap4j.packet.IpPacket
import org.pcap4j.packet.IpSelector
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class ToxVpnRunnable @Inject constructor(
    private val listenerCallbacks: EventListenerCallbacks,
    private var mTox: Tox,
    private var mService: ToxVpnService,
    private val mIface: ParcelFileDescriptor,
    private var mContactManager: ContactManager,
    private var routes: Map<InetAddress, Contact>
) : Runnable {
    val mtu: Int = mService.resources.getInteger(R.integer.mtu)
    val offset: Int = mService.resources.getInteger(R.integer.offset)

    interface OnConnectListener {
        enum class Stage {
            taskLaunch, connecting, establish, disconnected, taskTerminate
        }

        fun onConnectStage(stage: Stage?)
    }

    private val KEEPALIVE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(15)
    private val IDLE_INTERVAL_MS = TimeUnit.MILLISECONDS.toMillis(100)
    private val RECEIVE_TIMEOUT_MS: Long = KEEPALIVE_INTERVAL_MS * 3
    private val MAX_PACKET_SIZE = Short.MAX_VALUE.toInt()

    private var lastReadServerTime: Long = 0
    private var lastReadVirtualInterfaceTime: Long = 0


    private var mOnConnectListener: OnConnectListener? = null
    private var mConfigureIntent: PendingIntent? = null

    suspend fun findRouteByStatusMessage(address: InetAddress): Contact? {
        val a = address.hostAddress
        val s = "{\"ownip\":\"$a\"}"
        return mContactManager.getAll().firstOrNull { it.size > 0 }?.firstOrNull { it.statusMessage.equals(s) }
    }

    fun findRoute(address: InetAddress): Contact? {
        return routes.firstNotNullOfOrNull { it.takeIf { entry ->  entry.key.equals(address) }}?.value
    }

    override fun run() {
        try {
            Log.i(getTag(), "Thread starting")
            synchronized(mService) {
                mOnConnectListener?.onConnectStage(OnConnectListener.Stage.taskLaunch)
            }

            // We try to create the tunnel several times.
            // TODO: The better way is to work with ConnectivityManager, trying only when the
            //       network is available.
            // Here we just use a counter to keep things simple.
            var attempt = 0
            while (attempt < 10) {

                // Reset the counter if we were connected.
                if (_run()) {
                    attempt = 0
                }

                // Sleep for a while. This also checks if we got interrupted.
                Thread.sleep(3000)
                ++attempt
            }
            Log.i(getTag(), "Giving up")
        } catch (e: InterruptedException) {
            Log.i(getTag(), "Connection Interrupted, exiting")
        } catch (e: IllegalArgumentException) {
            Log.e(getTag(), "Connection failed, exiting", e)
        } catch (e: IllegalStateException) {
            Log.e(getTag(), "Connection failed, exiting", e)
        } finally {
            Log.i(getTag(), "Thread dying")
            synchronized(mService) {
                mOnConnectListener?.onConnectStage(OnConnectListener.Stage.taskTerminate)
            }
        }
    }

    private fun _run(): Boolean {
        var success = false

        try {
            synchronized(mService) {
                mOnConnectListener?.onConnectStage(OnConnectListener.Stage.connecting)
            }

            while (!mTox.started) Thread.sleep(100)

            synchronized(mService) {
                mOnConnectListener?.onConnectStage(OnConnectListener.Stage.establish)
            }

            // Now we are connected. Set the flag.
            success = true


            // Allocate the buffer for a single packet.
            val packet = ByteBuffer.allocate(MAX_PACKET_SIZE)

            // Packets to be sent are queued in this input stream.
            val ifaceIn: FileInputStream = FileInputStream(mIface!!.getFileDescriptor())

            // Packets received need to be written to this output stream.
            val ifaceOut: FileOutputStream = FileOutputStream(mIface!!.getFileDescriptor())

            listenerCallbacks.setLossyPacketHandler { publicKey, data ->
                if (data.size > 0) {
                    if(data[0].equals((200).toByte())) {
//                        val ippacket = IpSelector.newPacket(data, offset, data.size-offset);
//                        if (ippacket is IpPacket) {
//                            val dstAddr = ippacket.header.dstAddr
//                            val srcAddr = ippacket.header.srcAddr
//
//                            //Log.d(getTag(), "srcDest: $srcAddr, dstAddr: $dstAddr")
//                            //Log.d(getTag(), "ippacket.rawData.size: ${ippacket.rawData.size}")
//                            //Log.d(getTag(), "ippacket data: ${ippacket.rawData.joinToString("") { "%02x".format(it) }}")
                        ifaceOut.write(data, offset, data.size - offset)
                            lastReadServerTime = System.currentTimeMillis()
//                        }
                    }
                }
            }

                // We keep forwarding packets till something goes wrong.
                while (!Thread.interrupted()) {
                    // Assume that we did not make any progress in this iteration.
                    var idle = true

                    // Read the outgoing packet from the input stream (Virtual Interface).
                    var length = ifaceIn.read(packet.array())
                    if (length > 0) {
                        //Log.d(getTag(), "iface read length: $length")
                        // Write the outgoing packet to the tunnel (server).
                        packet.limit(length)
                        //Log.d(getTag(), "packet limit 1: ${packet.limit()}")

//                        val size = packet.array().size
                        //Log.d(getTag(), "packet size after limit: $size")
                        //Log.d(getTag(), "packet position 1: ${packet.position()}")

                        val ippacket = IpSelector.newPacket(packet.array(), 0, length);
                        //Log.d(getTag(), "packet position 2: ${packet.position()}")
                        if (ippacket is IpPacket) {
                            val dstAddr = ippacket.header.dstAddr
//                            val srcAddr = ippacket.header.srcAddr

                            //Log.d(getTag(), "srcDest: $srcAddr, dstAddr: $dstAddr")
                            //Log.d(getTag(), "ippacket.rawData.size: ${ippacket.rawData.size}")
                            //Log.d(getTag(), "ippacket data: ${ippacket.rawData.joinToString("") { "%02x".format(it) }}")
//                            try {
                            findRoute(dstAddr)?.apply {
                                val toxpacket = ByteBuffer.allocate(length + offset)
                                toxpacket.put((200).toByte())
                                toxpacket.put(0)
                                toxpacket.put(0)
                                toxpacket.put(8)
                                toxpacket.put(0)
                                toxpacket.put(packet)
                                //Log.d(getTag(), "toxpacket.array().size: ${toxpacket.array().size}")
                                //Log.d(getTag(), "data: ${toxpacket.array().joinToString("") { "%02x".format(it) }}")

                                val result = mTox.sendLossyPacket(PublicKey(publicKey), toxpacket.array())
                                //Log.d(getTag(), "result sendLosslessPacket: $result")
//                                mTox.sendMessage(PublicKey(publicKey), packet.array().size.toString(), MessageType.Normal)
                            } ?: run{
                                Log.w(getTag(), "No such address: $dstAddr")
                            }
//                            }catch (e: NoSuchElementException) {
//                                Log.w(getTag(), "No such address: $dstAddr")
//                            }
                        } else {
                            Log.w(getTag(), "datagram is not IpPacket")
                        }
                        packet.clear()
//                        val size2 = packet.array().size
                        //Log.d(getTag(), "packet size after clear: $size2")
                        //Log.d(getTag(), "packet limit after clear: ${packet.limit()}")

                        // There might be more outgoing packets.
                        idle = false
                        lastReadVirtualInterfaceTime = System.currentTimeMillis()
                    }

                    // If we are idle or waiting for the network, sleep for a
                    // fraction of time to avoid busy looping.
                    if (idle) {
                        Thread.sleep(IDLE_INTERVAL_MS)
                    }
                }
        } catch (e: IOException) {
            Log.e(getTag(), "Cannot use socket", e)
        } finally {
            synchronized(mService) {
                mOnConnectListener?.onConnectStage(OnConnectListener.Stage.disconnected)
            }
            try {
                Log.i(getTag(), "Close iface")
                mIface.close()
                listenerCallbacks.setLossyPacketHandler({ _, _ ->  } )
            } catch (e: Exception) {
                Log.e(getTag(), "Unable to close interface", e)
            }
        }
        return success
    }

    private fun getTag(): String? {
        return ToxVpnRunnable::class.java.getSimpleName()
    }

    fun setOnConnectListener(listener: OnConnectListener) {
        mOnConnectListener = listener
    }

    fun setConfigureIntent(intent: PendingIntent) {
        mConfigureIntent = intent
    }
}
