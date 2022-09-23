// SPDX-FileCopyrightText: 2019-2022 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import android.content.Context
import android.util.AtomicFile
import android.util.Log
import androidx.core.util.writeBytes
import java.io.File
import javax.inject.Inject

private const val TAG = "SaveManager"

class SaveManager @Inject constructor(val context: Context) {
    private val saveDir = context.filesDir

    init {
        if (!saveDir.exists()) {
            saveDir.mkdir()
        }
    }

    fun list(): List<String> = saveDir.listFiles()?.let { saves ->
        saves.filter { it.extension == "tox" }.map { it.nameWithoutExtension }
    } ?: listOf()

    fun save(publicKey: PublicKey, saveData: ByteArray) = AtomicFile(File("$saveDir/${publicKey.string()}.tox")).run {
        Log.i(TAG, "Saving profile to $baseFile")
        writeBytes(saveData)
    }

    fun load(publicKey: PublicKey): ByteArray? = tryReadBytes(File(pathTo(publicKey)))

    private fun tryReadBytes(saveFile: File): ByteArray? =
        if (saveFile.exists()) {
            saveFile.readBytes()
        } else {
            null
        }

    private fun pathTo(publicKey: PublicKey) = "$saveDir/${publicKey.string()}.tox"
}
