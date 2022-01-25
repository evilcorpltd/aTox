// SPDX-FileCopyrightText: 2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.av

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log

private const val TAG = "AudioCapture"

private fun intToChannel(channels: Int) = when (channels) {
    1 -> AudioFormat.CHANNEL_IN_MONO
    else -> AudioFormat.CHANNEL_IN_STEREO
}

// The permission linting doesn't work very well unless you sprinkle
// ContextCompat.checkSelfPermission in way too many places. It doesn't even
// agree with results from ActivityResultContracts.RequestPermission, requiring
// an extra permission check in there as well.
@SuppressLint("MissingPermission")
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
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            Log.w(TAG, "Failed to initialize audio record $audioSource")
            continue
        }
        return recorder
    }

    return null
}

class AudioCapture private constructor(
    private val sampleRate: Int,
    private val channels: Int,
    private val frameLengthMs: Int,
    private val audioRecord: AudioRecord,
) {
    fun start() = audioRecord.startRecording()
    fun stop() = audioRecord.stop()
    fun release() = audioRecord.release()
    fun read(): ShortArray {
        val bytes = ShortArray((sampleRate * channels * frameLengthMs / 1000.0).toInt())
        audioRecord.read(bytes, 0, bytes.size)
        return bytes
    }

    companion object {
        fun create(sampleRate: Int, channels: Int, frameLengthMs: Int): AudioCapture? {
            val audioRecord = findAudioRecord(sampleRate, channels) ?: return null
            return AudioCapture(sampleRate, channels, frameLengthMs, audioRecord)
        }
    }
}
