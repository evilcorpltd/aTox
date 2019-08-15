package ltd.evilcorp.atox.filetransfer

import android.content.Context
import android.util.Log
import ltd.evilcorp.atox.tox.ToxThread
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FileTransferRepository
import ltd.evilcorp.core.vo.FileTransfer
import java.io.File
import java.io.RandomAccessFile
import javax.inject.Inject

private const val TAG = "FileTransferManager"

class FileTransferManager @Inject constructor(
    private val context: Context,
    private val contactRepository: ContactRepository,
    private val fileTransferRepository: FileTransferRepository,
    private val tox: ToxThread
) {
    private val fileTransfers: MutableList<FileTransfer> = mutableListOf()

    fun accept(ft: FileTransfer) {
        fileTransfers.add(ft)
        fileTransferRepository.add(ft)

        val avatarFolder = File(context.filesDir, "avatar")
        if (!avatarFolder.exists()) {
            avatarFolder.mkdir()
        }

        RandomAccessFile(File(avatarFolder, ft.fileName), "rwd").apply {
            setLength(ft.fileSize)
            close()
        }

        // TODO(robinlinden): Get file ID from Tox and cancel transfer if we already have the file.
        tox.startFileTransfer(ft.publicKey, ft.fileNumber)
        contactRepository.setAvatarUri(ft.publicKey, File(avatarFolder, ft.fileName).toURI().toString())
    }

    fun reject(ft: FileTransfer) = tox.stopFileTransfer(ft.publicKey, ft.fileNumber)

    fun addDataToTransfer(contactPublicKey: String, fileNumber: Int, position: Long, data: ByteArray) {
        fileTransfers.find { it.publicKey == contactPublicKey && it.fileNumber == fileNumber }?.let { fileTransfer ->
            val avatarFolder = File(context.filesDir, "avatar")
            RandomAccessFile(File(avatarFolder, fileTransfer.fileName), "rwd").apply {
                seek(position)
                write(data)
                close()
            }
        } ?: Log.e(TAG, "Got chunk for file transfer $fileNumber for $contactPublicKey we don't know about")
    }
}
