package ltd.evilcorp.core.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.vo.FileTransfer

@Dao
internal interface FileTransferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(fileTransfer: FileTransfer)

    @Delete
    fun delete(fileTransfer: FileTransfer)

    @Query("SELECT * FROM file_transfers WHERE public_key == :publicKey AND file_number == :fileNumber")
    fun load(publicKey: String, fileNumber: Int): Flow<List<FileTransfer>>

    @Query(
        "UPDATE file_transfers SET progress = :progress WHERE public_key == :publicKey AND file_number == :fileNumber"
    )
    fun updateProgress(publicKey: String, fileNumber: Int, progress: Long)
}
