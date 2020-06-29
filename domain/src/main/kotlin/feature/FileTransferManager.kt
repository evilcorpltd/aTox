package ltd.evilcorp.domain.feature

import android.content.Context
import android.util.Log
import java.io.File
import java.io.RandomAccessFile
import javax.inject.Inject
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FileTransferRepository
import ltd.evilcorp.core.vo.FileKind
import ltd.evilcorp.core.vo.FileTransfer
import ltd.evilcorp.core.vo.isComplete
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox

private const val TAG = "FileTransferManager"

class FileTransferManager @Inject constructor(
    private val context: Context,
    private val contactRepository: ContactRepository,
    private val fileTransferRepository: FileTransferRepository,
    private val tox: Tox
) {
    private val fileTransfers: MutableList<FileTransfer> = mutableListOf()

    fun add(ft: FileTransfer) {
        when (ft.fileKind) {
            FileKind.Data.ordinal -> {
                // TODO(robinlinden): Add a chat message allowing the user to accept/reject the transfer.
                Log.e(TAG, "Ignoring non-avatar file transfer ${ft.fileNumber} (${ft.fileName}) from ${ft.publicKey}")
                reject(ft)
            }
            FileKind.Avatar.ordinal -> {
                if (ft.fileSize == 0L) {
                    contactRepository.setAvatarUri(ft.publicKey, "")
                    tox.stopFileTransfer(PublicKey(ft.publicKey), ft.fileNumber)
                    return
                } else if (ft.fileSize > 64 * 1024) {
                    Log.e(TAG, "Got trash avatar with size ${ft.fileSize} from ${ft.publicKey}")
                    contactRepository.setAvatarUri(ft.publicKey, "")
                    tox.stopFileTransfer(PublicKey(ft.publicKey), ft.fileNumber)
                    return
                }

                fileTransferRepository.add(ft)
                fileTransfers.add(ft)
                accept(ft)
            }
            else -> {
                Log.e(TAG, "Got unknown file kind ${ft.fileKind} in file transfer")
            }
        }
    }

    private fun accept(ft: FileTransfer) {
        val avatarFolder = File(context.filesDir, "avatar")
        if (!avatarFolder.exists()) {
            avatarFolder.mkdir()
        }

        RandomAccessFile(File(avatarFolder, ft.fileName), "rwd").apply {
            setLength(ft.fileSize)
            close()
        }

        // TODO(robinlinden): Get file ID from Tox and cancel transfer if we already have the file.
        tox.startFileTransfer(PublicKey(ft.publicKey), ft.fileNumber)
    }

    private fun reject(ft: FileTransfer) = tox.stopFileTransfer(PublicKey(ft.publicKey), ft.fileNumber)

    fun addDataToTransfer(publicKey: String, fileNumber: Int, position: Long, data: ByteArray) {
        fileTransfers.find { it.publicKey == publicKey && it.fileNumber == fileNumber }?.let { ft ->
            val avatarFolder = File(context.filesDir, "avatar")
            RandomAccessFile(File(avatarFolder, ft.fileName), "rwd").apply {
                seek(position)
                write(data)
                close()
            }

            fileTransferRepository.updateProgress(ft.publicKey, ft.fileNumber, ft.progress + data.size)
            fileTransfers[fileTransfers.indexOf(ft)] = ft.copy(progress = ft.progress + data.size)

            if (ft.isComplete()) {
                contactRepository.setAvatarUri(ft.publicKey, File(avatarFolder, ft.fileName).toURI().toString())
            }
        } ?: Log.e(TAG, "Got chunk for file transfer $fileNumber for $publicKey we don't know about")
    }
}
