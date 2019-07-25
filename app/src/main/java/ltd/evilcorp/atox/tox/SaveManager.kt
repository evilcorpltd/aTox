package ltd.evilcorp.atox.tox

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

    fun list(): List<String> = saveDir.listFiles().filter { it.extension == "tox" }.map { it.nameWithoutExtension }

    fun save(publicKey: String, saveData: ByteArray) = File("$saveDir/$publicKey.tox").run {
        if (!exists()) {
            createNewFile()
        }

        Log.i(TAG, "Saving profile to $this")
        writeBytes(saveData)
    }

    fun load(publicKey: String): ByteArray? = tryReadBytes(File("$saveDir/$publicKey.tox"))

    private fun tryReadBytes(saveFile: File): ByteArray? =
        if (saveFile.exists()) {
            saveFile.readBytes()
        } else {
            null
        }
}
