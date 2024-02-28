package com.zktony.android.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zktony.android.data.entities.ExperimentRecord
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class ExperimentRecordDao : BaseDao<ExperimentRecord> {
    @Query(
        """
        SELECT * FROM ExperimentRecord
        ORDER BY createTime DESC
        """
    )
    abstract fun getAll(): Flow<List<ExperimentRecord>>

    @Query(
        """
        SELECT * FROM ExperimentRecord
        ORDER BY id DESC
        """
    )
    abstract fun getByPage(): PagingSource<Int, ExperimentRecord>

    @Query(
        """
        SELECT * FROM ExperimentRecord
        WHERE id = :id
        """
    )
    abstract fun getById(id: Long): Flow<ExperimentRecord?>

    @Query(
        """
        DELETE FROM ExperimentRecord
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)

    @Query(
        """
        DELETE FROM sportslog
        """
    )
    abstract suspend fun deleteByAll()
}