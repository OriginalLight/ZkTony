package com.zktony.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.zktony.room.entities.LogSnapshot
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LogSnapshotDao : BaseDao<LogSnapshot> {
    @Query(
        """
        DELETE FROM log_snapshots
        WHERE subId = :subId
    """
    )
    abstract fun deleteBySubId(subId: Long): Int

    @Query(
        """
        SELECT * FROM log_snapshots
        WHERE subId = :subId
    """
    )
    abstract fun getBySubId(subId: Long): Flow<List<LogSnapshot>>
}