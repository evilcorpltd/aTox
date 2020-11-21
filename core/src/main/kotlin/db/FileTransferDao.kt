package ltd.evilcorp.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.vo.FileTransfer
import ltd.evilcorp.core.vo.FtRejected

@Dao
internal interface FileTransferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(fileTransfer: FileTransfer): Long

    @Query("DELETE FROM file_transfers WHERE id == :id")
    fun delete(id: Int)

    @Query("SELECT * FROM file_transfers WHERE public_key == :publicKey")
    fun load(publicKey: String): Flow<List<FileTransfer>>

    @Query("UPDATE file_transfers SET progress = :progress WHERE id == :id")
    fun updateProgress(id: Int, progress: Long)

    @Query("UPDATE file_transfers SET destination = :destination WHERE id == :id")
    fun setDestination(id: Int, destination: String)

    @Query("UPDATE file_transfers SET progress = :progress WHERE progress < file_size")
    fun resetTransientData(progress: Long = FtRejected)
}
