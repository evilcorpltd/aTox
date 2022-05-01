// SPDX-FileCopyrightText: 2019 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import android.util.Log
import java.io.File
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.DIContext
import org.kodein.di.instance

private const val TAG = "SaveManager"

class SaveManagerImpl(override val di: DI, override val diContext: DIContext<*>) : SaveManager, DIAware {
    private val saveDir: File by instance(tag = "files")

    init {
        if (!saveDir.exists()) {
            saveDir.mkdir()
        }
    }

    override fun list(): List<String> = saveDir.listFiles()?.let { saves ->
        saves.filter { it.extension == "tox" }.map { it.nameWithoutExtension }
    } ?: listOf()

    override fun save(publicKey: PublicKey, saveData: ByteArray) = File("$saveDir/${publicKey.string()}.tox").run {
        if (!exists()) {
            createNewFile()
        }

        Log.i(TAG, "Saving profile to $this")
        writeBytes(saveData)
    }

    override fun load(publicKey: PublicKey): ByteArray? = tryReadBytes(File(pathTo(publicKey)))

    private fun tryReadBytes(saveFile: File): ByteArray? =
        if (saveFile.exists()) {
            saveFile.readBytes()
        } else {
            null
        }

    private fun pathTo(publicKey: PublicKey) = "$saveDir/${publicKey.string()}.tox"
}
