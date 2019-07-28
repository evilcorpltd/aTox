package ltd.evilcorp.core.repository

import androidx.lifecycle.LiveData
import ltd.evilcorp.core.db.FileTransferDao
import ltd.evilcorp.core.vo.FileTransfer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileTransferRepository @Inject internal constructor(
    private val FileTransferDao: FileTransferDao
) {
    fun add(FileTransfer: FileTransfer) =
        FileTransferDao.save(FileTransfer)

    fun delete(FileTransfer: FileTransfer) =
        FileTransferDao.delete(FileTransfer)

    fun get(publicKey: String, fileNumber: Int): LiveData<List<FileTransfer>> =
        FileTransferDao.load(publicKey, fileNumber)
}
