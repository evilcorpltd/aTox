package ltd.evilcorp.core.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.db.FileTransferDao
import ltd.evilcorp.core.vo.FileTransfer

@Singleton
class FileTransferRepository @Inject internal constructor(
    private val dao: FileTransferDao
) {
    fun add(ft: FileTransfer): Long =
        dao.save(ft)

    fun delete(ft: FileTransfer) =
        dao.delete(ft)

    fun get(publicKey: String): Flow<List<FileTransfer>> =
        dao.load(publicKey)

    fun setDestination(id: Int, destination: String) =
        dao.setDestination(id, destination)

    fun updateProgress(id: Int, progress: Long) =
        dao.updateProgress(id, progress)
}
