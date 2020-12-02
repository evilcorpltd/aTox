package ltd.evilcorp.domain.feature

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import im.tox.tox4j.core.enums.ToxFileControl
import java.io.File
import java.io.RandomAccessFile
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FileTransferRepository
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.vo.FileKind
import ltd.evilcorp.core.vo.FileTransfer
import ltd.evilcorp.core.vo.FtNotStarted
import ltd.evilcorp.core.vo.FtRejected
import ltd.evilcorp.core.vo.FtStarted
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.Sender
import ltd.evilcorp.core.vo.isComplete
import ltd.evilcorp.core.vo.isStarted
import ltd.evilcorp.domain.tox.MAX_AVATAR_SIZE
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox

private const val TAG = "FileTransferManager"

@Singleton
class FileTransferManager @Inject constructor(
    private val context: Context,
    private val resolver: ContentResolver,
    private val contactRepository: ContactRepository,
    private val messageRepository: MessageRepository,
    private val fileTransferRepository: FileTransferRepository,
    private val tox: Tox
) {
    private val fileTransfers: MutableList<FileTransfer> = mutableListOf()

    init {
        File(context.filesDir, "ft").mkdir()
    }

    fun reset() {
        fileTransfers.clear()
        GlobalScope.launch {
            fileTransferRepository.resetTransientData()
        }
    }

    fun resetForContact(pk: String) {
        Log.i(TAG, "Clearing fts for contact ${pk.take(8)}")
        fileTransfers.filter { it.publicKey == pk }.forEach { ft ->
            setProgress(ft, FtRejected)
            fileTransfers.remove(ft)
            File(ft.destination).delete()
        }
    }

    fun add(ft: FileTransfer) {
        Log.i(TAG, "Add ${ft.fileNumber} for ${ft.publicKey.take(8)}")
        when (ft.fileKind) {
            FileKind.Data.ordinal -> {
                val id = fileTransferRepository.add(ft).toInt()
                messageRepository.add(
                    Message(
                        ft.publicKey,
                        ft.fileName,
                        Sender.Received,
                        MessageType.FileTransfer,
                        id,
                        Date().time
                    )
                )
                fileTransfers.add(ft.copy().apply { this.id = id })
            }
            FileKind.Avatar.ordinal -> {
                if (ft.fileSize == 0L) {
                    contactRepository.setAvatarUri(ft.publicKey, "")
                    reject(ft)
                    return
                } else if (ft.fileSize > MAX_AVATAR_SIZE) {
                    Log.e(TAG, "Got trash avatar with size ${ft.fileSize} from ${ft.publicKey}")
                    contactRepository.setAvatarUri(ft.publicKey, "")
                    tox.stopFileTransfer(PublicKey(ft.publicKey), ft.fileNumber)
                    return
                }

                fileTransfers.add(ft)
                accept(ft)
            }
            else -> {
                Log.e(TAG, "Got unknown file kind ${ft.fileKind} in file transfer")
            }
        }
    }

    fun accept(id: Int) {
        fileTransfers.find { it.id == id }?.let {
            accept(it)
        } ?: Log.e(TAG, "Unable to find & accept ft $id")
    }

    fun accept(ft: FileTransfer) {
        Log.i(TAG, "Accept ${ft.fileNumber} for ${ft.publicKey.take(8)}")
        when (ft.fileKind) {
            FileKind.Data.ordinal -> {
                val dest = makeDestination(ft)
                setDestination(ft, dest)
                val file = File(dest.path!!)
                file.parentFile!!.mkdirs()
                RandomAccessFile(file, "rwd").run {
                    setLength(ft.fileSize)
                    close()
                }
            }
            FileKind.Avatar.ordinal -> {
                val folder = File(context.filesDir, "avatar")
                if (!folder.exists()) {
                    folder.mkdir()
                }
                RandomAccessFile(File(folder, ft.fileName), "rwd").apply {
                    setLength(ft.fileSize)
                    close()
                }
                setDestination(ft, Uri.fromFile(File(folder, ft.fileName)))
            }
            else -> {
                Log.e(TAG, "Got unknown file kind when accepting ft: $ft")
                return
            }
        }

        setProgress(ft, FtStarted)
        tox.startFileTransfer(PublicKey(ft.publicKey), ft.fileNumber)
    }

    fun reject(id: Int) {
        fileTransfers.find { it.id == id }?.let {
            reject(it)
        } ?: Log.e(TAG, "Unable to find & reject ft $id")
    }

    fun reject(ft: FileTransfer) {
        Log.i(TAG, "Reject ${ft.fileNumber} for ${ft.publicKey.take(8)}")
        fileTransfers.remove(ft)
        setProgress(ft, FtRejected)
        tox.stopFileTransfer(PublicKey(ft.publicKey), ft.fileNumber)
        File(Uri.parse(ft.destination).path!!).delete()
    }

    private fun setDestination(ft: FileTransfer, destination: Uri) {
        fileTransfers[fileTransfers.indexOf(ft)].destination = destination.toString()
        if (ft.fileKind == FileKind.Data.ordinal) {
            fileTransferRepository.setDestination(ft.id, destination.toString())
        }
    }

    private fun setProgress(ft: FileTransfer, progress: Long) {
        val id = fileTransfers.indexOf(ft)
        fileTransfers.elementAtOrNull(id)?.progress = progress
        if (ft.fileKind == FileKind.Data.ordinal) {
            fileTransferRepository.updateProgress(ft.id, progress)
        }
    }

    fun addDataToTransfer(publicKey: String, fileNumber: Int, position: Long, data: ByteArray) {
        val ft = fileTransfers.find { it.publicKey == publicKey && it.fileNumber == fileNumber }
        if (ft == null) {
            if (data.isNotEmpty()) {
                Log.e(TAG, "Got data for ft $fileNumber for ${publicKey.take(8)} we don't know about")
            }
            return
        }

        if (ft.fileKind != FileKind.Data.ordinal && ft.fileKind != FileKind.Avatar.ordinal) {
            Log.e(TAG, "Got unknown file kind when adding data to ft: $ft")
            return
        }

        RandomAccessFile(File(Uri.parse(ft.destination).path!!), "rwd").apply {
            seek(position)
            write(data)
            close()
        }

        setProgress(ft, ft.progress + data.size)

        if (ft.isComplete()) {
            Log.i(TAG, "Finished ${ft.fileNumber} for ${ft.publicKey.take(8)}")
            if (ft.fileKind == FileKind.Avatar.ordinal) {
                contactRepository.setAvatarUri(ft.publicKey, ft.destination)
            }
            fileTransfers.remove(ft)
        }
    }

    fun transfersFor(publicKey: PublicKey) = fileTransferRepository.get(publicKey.string())

    suspend fun create(pk: PublicKey, file: Uri) {
        val cursor = context.contentResolver.query(file, null, null, null, null, null)
        if (cursor == null) {
            Log.e(TAG, "oh no")
            return
        }

        cursor.moveToFirst()
        val fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        cursor.close()
        val ft = FileTransfer(
            pk.string(),
            tox.sendFile(pk, FileKind.Data, fileSize, name).await(),
            FileKind.Data.ordinal,
            fileSize,
            name,
            true,
            FtNotStarted,
            file.toString()
        )
        val id = fileTransferRepository.add(ft).toInt()
        messageRepository.add(
            Message(
                ft.publicKey,
                ft.fileName,
                Sender.Sent,
                MessageType.FileTransfer,
                id,
                Date().time
            )
        )
        fileTransfers.add(ft.copy().apply { this.id = id })
    }

    fun sendChunk(pk: String, fileNo: Int, pos: Long, length: Int) {
        val ft = fileTransfers.find { it.publicKey == pk && it.fileNumber == fileNo }
        if (ft == null) {
            Log.e(TAG, "Received request for chunk of unknown ft ${pk.take(8)} $fileNo")
            tox.stopFileTransfer(PublicKey(pk), fileNo)
            return
        }

        if (length == 0) {
            if (!ft.isComplete()) {
                Log.e(TAG, "Got a request for 0-sized chunk before ft was done ${pk.take(8)} $fileNo")
                return
            }

            Log.i(TAG, "Finished outgoing ft ${pk.take(8)} $fileNo")
            fileTransfers.remove(ft)
            return
        }

        val src = Uri.parse(ft.destination)
        val istream = resolver.openInputStream(src) ?: return
        istream.skip(pos)
        val bytes = ByteArray(length)
        istream.read(bytes, 0, length)
        istream.close()
        tox.sendFileChunk(PublicKey(pk), fileNo, pos, bytes)
        setProgress(ft, ft.progress + length)
    }

    fun setStatus(pk: String, fileNo: Int, fileStatus: ToxFileControl) {
        Log.e(TAG, "Setting ${pk.take(8)} $fileNo to status $fileStatus")
        val ft = fileTransfers.find { it.publicKey == pk && it.fileNumber == fileNo }
        if (ft == null) {
            Log.e(TAG, "Attempted to set status for unknown ft ${pk.take(8)} $fileNo")
            return
        }

        if (fileStatus == ToxFileControl.RESUME && ft.progress == FtNotStarted) {
            ft.progress = FtStarted
        } else if (fileStatus == ToxFileControl.CANCEL) {
            Log.i(TAG, "Friend canceled ft ${pk.take(8)} $fileNo")
            reject(ft)
        }
    }

    suspend fun delete(id: Int) {
        fileTransfers.find { it.id == id }?.let {
            if (it.isStarted() && !it.isComplete()) { reject(it) }
            fileTransfers.remove(it)
        }
        fileTransferRepository.get(id).take(1).collect {
            if (!it.outgoing && it.destination.startsWith("file://")) {
                File(Uri.parse(it.destination).path!!).delete()
            }
            fileTransferRepository.delete(id)
        }
    }

    fun get(id: Int) = fileTransferRepository.get(id)

    private fun makeDestination(ft: FileTransfer) =
        Uri.fromFile(File(File(File(context.filesDir, "ft"), ft.publicKey.take(8)), Random.nextLong().toString()))
}
