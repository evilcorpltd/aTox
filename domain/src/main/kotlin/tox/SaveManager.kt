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

private const val TAG = "AndroidSaveManager"

interface SaveManager {
    fun list(): List<String>
    fun save(pk: PublicKey, saveData: ByteArray)
    fun load(pk: PublicKey): ByteArray?
}

class AndroidSaveManager @Inject constructor(val context: Context) : SaveManager {
    private val saveDir get() = context.filesDir

    override fun list(): List<String> =
        saveDir.listFiles()?.filter { it.extension == "tox" }?.map(File::nameWithoutExtension) ?: listOf()

    override fun save(pk: PublicKey, saveData: ByteArray) = AtomicFile(fileFor(pk)).run {
        Log.i(TAG, "Saving profile to $baseFile")
        writeBytes(saveData)
    }

    override fun load(pk: PublicKey): ByteArray? = fileFor(pk).let { saveFile ->
        if (saveFile.exists()) {
            saveFile.readBytes()
        } else {
            null
        }
    }

    private fun fileFor(pk: PublicKey) = File("$saveDir/${pk.string()}.tox")
}
