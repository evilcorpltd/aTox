// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.feature

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import im.tox.tox4j.core.enums.ToxFileControl
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.forEach as kForEach
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FileTransferRepository
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.vo.FT_NOT_STARTED
import ltd.evilcorp.core.vo.FT_REJECTED
import ltd.evilcorp.core.vo.FT_STARTED
import ltd.evilcorp.core.vo.FileKind
import ltd.evilcorp.core.vo.FileTransfer
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.core.vo.Sender
import ltd.evilcorp.core.vo.isComplete
import ltd.evilcorp.core.vo.isStarted
import ltd.evilcorp.domain.tox.MAX_AVATAR_SIZE
import ltd.evilcorp.domain.tox.Tox

private const val TAG = "FileTransferManager"

@Suppress("ArrayInDataClass")
private data class Chunk(val pos: Long, val data: ByteArray)

private data class OutgoingFile(val inputStream: InputStream, val unsentChunks: MutableList<Chunk>)

@Singleton
class FileTransferManager @Inject constructor(
    private val scope: CoroutineScope,
    private val context: Context,
    private val resolver: ContentResolver,
    private val contactRepository: ContactRepository,
    private val messageRepository: MessageRepository,
    private val fileTransferRepository: FileTransferRepository,
    private val tox: Tox,
) {
    private val fileTransfers: MutableList<FileTransfer> = mutableListOf()
    private val outgoingFiles = mutableMapOf<Pair<PublicKey, Int>, OutgoingFile>()

    init {
        File(context.filesDir, "ft").mkdir()
        File(context.filesDir, "avatar").mkdir()
        resolver.persistedUriPermissions.kForEach {
            Log.w(TAG, "Clearing leftover permission for ${it.uri}")
            releaseFilePermission(it.uri)
        }
    }

    fun reset() {
        fileTransfers.clear()
        scope.launch {
            fileTransferRepository.resetTransientData()
        }
    }

    fun resetForContact(pk: PublicKey) {
        Log.i(TAG, "Clearing fts for contact ${pk.fingerprint()}")
        fileTransfers.filter { it.publicKey == pk }.kForEach { ft ->
            setProgress(ft, FT_REJECTED)
            fileTransfers.remove(ft)
            if (ft.outgoing) {
                val uri = Uri.parse(ft.destination)
                outgoingFiles.remove(Pair(pk, ft.fileNumber))?.inputStream?.close()
                releaseFilePermission(uri)
            } else {
                File(ft.destination).delete()
            }
        }
    }

    fun add(ft: FileTransfer): Int {
        Log.i(TAG, "Add ${ft.fileNumber} for ${ft.publicKey.fingerprint()}")
        return when (ft.fileKind) {
            FileKind.Data.ordinal -> {
                val id = fileTransferRepository.add(ft).toInt()
                messageRepository.add(
                    Message(ft.publicKey, ft.fileName, Sender.Received, MessageType.FileTransfer, id, Date().time),
                )
                fileTransfers.add(ft.copy().apply { this.id = id })
                id
            }
            FileKind.Avatar.ordinal -> {
                if (ft.fileSize == 0L) {
                    contactRepository.setAvatarUri(ft.publicKey, "")
                    reject(ft)
                    return -1
                } else if (ft.fileSize > MAX_AVATAR_SIZE) {
                    Log.e(TAG, "Got trash avatar with size ${ft.fileSize} from ${ft.publicKey}")
                    contactRepository.setAvatarUri(ft.publicKey, "")
                    tox.stopFileTransfer(ft.publicKey, ft.fileNumber)
                    return -1
                }

                fileTransfers.add(ft)
                accept(ft)
                -1
            }
            else -> {
                Log.e(TAG, "Got unknown file kind ${ft.fileKind} in file transfer")
                -1
            }
        }
    }

    fun accept(id: Int) {
        fileTransfers.find { it.id == id }?.let {
            accept(it)
        } ?: Log.e(TAG, "Unable to find & accept ft $id")
    }

    fun accept(ft: FileTransfer) {
        Log.i(TAG, "Accept ${ft.fileNumber} for ${ft.publicKey.fingerprint()}")
        val file = when (ft.fileKind) {
            FileKind.Data.ordinal -> {
                val dest = makeDestination(ft)
                val file = File(dest.path!!)
                file.parentFile!!.mkdirs()
                file
            }
            FileKind.Avatar.ordinal -> wipAvatar(ft.fileName)
            else -> {
                Log.e(TAG, "Got unknown file kind when accepting ft: $ft")
                return
            }
        }

        RandomAccessFile(file, "rwd").use { it.setLength(ft.fileSize) }
        setDestination(ft, Uri.fromFile(file))
        setProgress(ft, FT_STARTED)
        tox.startFileTransfer(ft.publicKey, ft.fileNumber)
    }

    fun reject(id: Int) {
        fileTransfers.find { it.id == id }?.let {
            reject(it)
        } ?: Log.e(TAG, "Unable to find & reject ft $id")
    }

    fun reject(ft: FileTransfer) {
        Log.i(TAG, "Reject ${ft.fileNumber} for ${ft.publicKey.fingerprint()}")
        fileTransfers.remove(ft)
        setProgress(ft, FT_REJECTED)
        tox.stopFileTransfer(ft.publicKey, ft.fileNumber)
        val uri = Uri.parse(ft.destination)
        if (ft.outgoing) {
            outgoingFiles.remove(Pair(ft.publicKey, ft.fileNumber))?.inputStream?.close()
            releaseFilePermission(uri)
        } else {
            File(uri.path!!).delete()
        }
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

    fun addDataToTransfer(publicKey: PublicKey, fileNumber: Int, position: Long, data: ByteArray) {
        val ft = fileTransfers.find { it.publicKey == publicKey && it.fileNumber == fileNumber }
        if (ft == null) {
            if (data.isNotEmpty()) {
                Log.e(TAG, "Got data for ft $fileNumber for ${publicKey.fingerprint()} we don't know about")
            }
            return
        }

        if (ft.fileKind != FileKind.Data.ordinal && ft.fileKind != FileKind.Avatar.ordinal) {
            Log.e(TAG, "Got unknown file kind when adding data to ft: $ft")
            return
        }

        RandomAccessFile(File(Uri.parse(ft.destination).path!!), "rwd").use {
            it.seek(position)
            it.write(data)
        }

        setProgress(ft, ft.progress + data.size)

        if (ft.isComplete()) {
            Log.i(TAG, "Finished ${ft.fileNumber} for ${ft.publicKey.fingerprint()}")
            if (ft.fileKind == FileKind.Avatar.ordinal) {
                wipAvatar(ft.fileName).copyTo(avatar(ft.fileName), overwrite = true)
                wipAvatar(ft.fileName).delete()
                contactRepository.setAvatarUri(ft.publicKey, Uri.fromFile(avatar(ft.fileName)).toString())
            }
            fileTransfers.remove(ft)
        }
    }

    fun transfersFor(publicKey: PublicKey) = fileTransferRepository.get(publicKey)

    fun create(pk: PublicKey, file: Uri) {
        val (name, size) = context.contentResolver.query(file, null, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            val fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            Pair(name, fileSize)
        } ?: return

        val ft = FileTransfer(
            pk,
            tox.sendFile(pk, FileKind.Data, size, name),
            FileKind.Data.ordinal,
            size,
            name,
            true,
            FT_NOT_STARTED,
            file.toString(),
        )
        val id = fileTransferRepository.add(ft).toInt()
        messageRepository.add(
            Message(ft.publicKey, ft.fileName, Sender.Sent, MessageType.FileTransfer, id, Date().time),
        )
        fileTransfers.add(ft.copy().apply { this.id = id })

        val inputStream = resolver.openInputStream(file)
        if (inputStream == null) {
            reject(ft)
            return
        }
        outgoingFiles[Pair(ft.publicKey, ft.fileNumber)] = OutgoingFile(inputStream, mutableListOf())
    }

    // TODO(robinlinden): Handle seek-backs: https://github.com/TokTok/c-toxcore/blob/eeaa039222e7a123c2585c8486ee965017767209/toxcore/tox.h#L2405-L2406
    // TODO(robinlinden): An error when sending the last chunk in a transfer will stall it.
    fun sendChunk(pk: PublicKey, fileNo: Int, pos: Long, length: Int) {
        val ft = fileTransfers.find { it.publicKey == pk && it.fileNumber == fileNo }
        if (ft == null) {
            Log.e(TAG, "Received request for chunk of unknown ft ${pk.fingerprint()} $fileNo")
            tox.stopFileTransfer(pk, fileNo)
            return
        }

        if (length == 0) {
            Log.i(TAG, "Finished outgoing ft ${pk.fingerprint()} $fileNo ${ft.isComplete()}")
            fileTransfers.remove(ft)
            outgoingFiles.remove(Pair(pk, fileNo))?.inputStream?.close()
            releaseFilePermission(Uri.parse(ft.destination))
            return
        }

        val file = outgoingFiles[Pair(pk, fileNo)] ?: return

        while (file.unsentChunks.isNotEmpty()) {
            val chunk = file.unsentChunks.first()
            Log.i(TAG, "Resending chunk @ ${chunk.pos} to ${pk.fingerprint()} ($fileNo)}")
            if (tox.sendFileChunk(pk, fileNo, chunk.pos, chunk.data).isFailure) {
                return
            }
            setProgress(ft, ft.progress + chunk.data.size)
            file.unsentChunks.removeAt(0)
        }

        val bytes = ByteArray(length)
        file.inputStream.read(bytes, 0, length)
        if (tox.sendFileChunk(pk, fileNo, pos, bytes).isFailure) {
            file.unsentChunks.add(Chunk(pos, bytes))
            return
        }

        setProgress(ft, ft.progress + length)
    }

    fun setStatus(pk: PublicKey, fileNo: Int, fileStatus: ToxFileControl) {
        Log.e(TAG, "Setting ${pk.fingerprint()} $fileNo to status $fileStatus")
        val ft = fileTransfers.find { it.publicKey == pk && it.fileNumber == fileNo }
        if (ft == null) {
            Log.e(TAG, "Attempted to set status for unknown ft ${pk.fingerprint()} $fileNo")
            return
        }

        if (fileStatus == ToxFileControl.RESUME && ft.progress == FT_NOT_STARTED) {
            ft.progress = FT_STARTED
        } else if (fileStatus == ToxFileControl.CANCEL) {
            Log.i(TAG, "Friend canceled ft ${pk.fingerprint()} $fileNo")
            reject(ft)
        }
    }

    suspend fun deleteAll(publicKey: PublicKey) {
        fileTransferRepository.get(publicKey).take(1).collect { fts ->
            fts.kForEach { delete(it.id) }
        }
    }

    suspend fun delete(id: Int) {
        fileTransfers.find { it.id == id }?.let {
            if (it.isStarted() && !it.isComplete()) {
                reject(it)
            }
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

    private fun releaseFilePermission(uri: Uri) {
        if (fileTransfers.firstOrNull { it.destination == uri.toString() } != null) {
            return
        }

        Log.i(TAG, "Releasing read permission for $uri")
        resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun makeDestination(ft: FileTransfer) =
        Uri.fromFile(File(File(File(context.filesDir, "ft"), ft.publicKey.fingerprint()), Random.nextLong().toString()))

    private fun wipAvatar(name: String): File = File(File(context.filesDir, "avatar"), "$name.wip")
    private fun avatar(name: String): File = File(File(context.filesDir, "avatar"), name)
}
