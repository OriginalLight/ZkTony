package com.zktony.android.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zktony.android.data.entities.ErrorRecord
import com.zktony.android.data.entities.ExperimentRecord
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class ErrorRecordDao : BaseDao<ErrorRecord> {
    @Query(
        """
        SELECT * FROM errorrecord
        ORDER BY createTime DESC
        """
    )
    abstract fun getAll(): Flow<List<ErrorRecord>>

    @Query(
        """
        SELECT * FROM errorrecord
        ORDER BY id DESC
        """
    )
    abstract fun getByPage(): PagingSource<Int, ErrorRecord>

    @Query(
        """
        SELECT * FROM errorrecord
        WHERE id = :id
        """
    )
    abstract fun getById(id: Long): Flow<ErrorRecord?>


    @Query(
        """
        DELETE FROM errorrecord
        """
    )
    abstract suspend fun deleteByAll()

    @Query(
        """
        DELETE FROM errorrecord
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)

}