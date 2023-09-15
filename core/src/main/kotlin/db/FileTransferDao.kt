// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.vo.FT_REJECTED
import ltd.evilcorp.core.vo.FileTransfer

@Dao
interface FileTransferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(fileTransfer: FileTransfer): Long

    @Query("DELETE FROM file_transfers WHERE id == :id")
    fun delete(id: Int)

    @Query("SELECT * FROM file_transfers WHERE public_key == :publicKey")
    fun load(publicKey: String): Flow<List<FileTransfer>>

    @Query("SELECT * FROM file_transfers WHERE id == :id")
    fun load(id: Int): Flow<FileTransfer>

    @Query("UPDATE file_transfers SET progress = :progress WHERE id == :id AND progress != :rejected")
    fun updateProgress(id: Int, progress: Long, rejected: Long = FT_REJECTED)

    @Query("UPDATE file_transfers SET destination = :destination WHERE id == :id")
    fun setDestination(id: Int, destination: String)

    @Query("UPDATE file_transfers SET progress = :progress WHERE progress < file_size")
    fun resetTransientData(progress: Long = FT_REJECTED)
}
