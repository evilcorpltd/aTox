package ltd.evilcorp.domain.av

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

private fun intToChannel(channels: Int) = when (channels) {
    1 -> AudioFormat.CHANNEL_IN_MONO
    else -> AudioFormat.CHANNEL_IN_STEREO
}

private fun findAudioRecord(sampleRate: Int, channels: Int): AudioRecord? {
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val channelConfig = intToChannel(channels)

    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    if (bufferSize == AudioRecord.ERROR_BAD_VALUE) {
        return null
    }

    // Seems like not all Xiaomi phones have a VOICE_COMMUNICATION audio source, so try a few different ones.
    val audioSources = arrayOf(
        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
        MediaRecorder.AudioSource.MIC,
        MediaRecorder.AudioSource.DEFAULT,
    )
    for (audioSource in audioSources) {
        val recorder = AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize)
        if (recorder.state == AudioRecord.STATE_INITIALIZED) {
            return recorder
        }
    }

    return null
}

class AudioCapture(private val sampleRate: Int, private val channels: Int) {
    private val audioRecord = findAudioRecord(sampleRate, channels)
    fun isOk() = audioRecord != null
    fun start() = audioRecord?.startRecording()
    fun stop() = audioRecord?.stop()
    fun release() = audioRecord?.release()
    fun read(): ShortArray {
        val bytes = ShortArray((sampleRate * channels * 0.1).toInt()) // E.g. 16-bit, 48kHz, 1 channel, 100ms
        audioRecord?.read(bytes, 0, bytes.size)
        return bytes
    }
}
