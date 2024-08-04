package com.zktony.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.zktony.room.entities.Log
import kotlinx.coroutines.flow.Flow

/**
 * @author 刘贺贺
 * @date 2023/8/30 10:56
 */
@Dao
abstract class LogDao : BaseDao<Log> {
    @Query(
        """
        SELECT * FROM logs
        ORDER BY createTime DESC
        """
    )
    abstract fun getAll(): Flow<List<Log>>

    @Query(
        """
        SELECT * FROM logs
        ORDER BY createTime DESC
        """
    )
    abstract fun getByPage(): PagingSource<Int, Log>

    @Query(
        """
        DELETE FROM logs
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}