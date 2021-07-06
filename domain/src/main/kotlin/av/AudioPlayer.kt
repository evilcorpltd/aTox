package ltd.evilcorp.domain.av

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build

private fun intToChannel(channels: Int) = when (channels) {
    1 -> AudioFormat.CHANNEL_OUT_MONO
    else -> AudioFormat.CHANNEL_OUT_STEREO
}

class AudioPlayer(sampleRate: Int, channels: Int) {
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
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize)
            .build()
    }

    fun buffer(data: ShortArray) {
        audioTrack.write(data, 0, data.size)
    }

    fun start() {
        audioTrack.play()
    }

    fun stop() {
        audioTrack.pause()
        audioTrack.flush()
    }

    fun release() = audioTrack.release()
}
