package ltd.evilcorp.domain.av

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun intToChannel(channels: Int) = when (channels) {
    1 -> AudioFormat.CHANNEL_OUT_MONO
    else -> AudioFormat.CHANNEL_OUT_STEREO
}

class AudioPlayer(private val sampleRate: Int, channels: Int) : CoroutineScope by GlobalScope {
    private val minBufferSize =
        AudioTrack.getMinBufferSize(sampleRate, intToChannel(channels), AudioFormat.ENCODING_PCM_16BIT)
    private val audioTrack = if (Build.VERSION.SDK_INT < 23) {
        // TODO(robinlinden): Verify that this works on old devices.
        AudioTrack(
            AudioManager.STREAM_VOICE_CALL,
            sampleRate,
            intToChannel(channels),
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize,
            AudioTrack.MODE_STREAM
        )
    } else {
        AudioTrack.Builder()
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(intToChannel(channels))
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize)
            .build()
    }
    private val audioQueue: Queue<ShortArray> = ConcurrentLinkedQueue()

    private var active = false

    fun buffer(data: ShortArray) = audioQueue.add(data)

    fun start() {
        active = true
        launch {
            audioTrack.play()
            while (active) {
                val sleepTime = playAudioFrame()
                delay(sleepTime.toLong())
            }
            audioTrack.pause()
            audioTrack.flush()
        }
    }

    fun stop() {
        active = false
    }

    private fun playAudioFrame(): Int = if (audioQueue.isEmpty()) {
        0
    } else {
        val data = audioQueue.remove()
        audioTrack.write(data, 0, data.size)
        data.size * 1000 / sampleRate
    }
}
