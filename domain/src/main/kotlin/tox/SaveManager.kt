// SPDX-FileCopyrightText: 2019 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import android.content.Context
import android.util.Log
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

    fun save(publicKey: PublicKey, saveData: ByteArray) = File("$saveDir/${publicKey.string()}.tox").run {
        if (!exists()) {
            createNewFile()
        }

        Log.i(TAG, "Saving profile to $this")
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
