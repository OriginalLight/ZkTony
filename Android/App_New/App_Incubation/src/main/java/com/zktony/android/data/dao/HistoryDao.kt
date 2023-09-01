package com.zktony.android.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zktony.android.data.entities.History
import kotlinx.coroutines.flow.Flow

/**
 * @author 刘贺贺
 * @date 2023/8/30 10:56
 */
@Dao
abstract class HistoryDao : BaseDao<History> {
    @Query(
        """
        SELECT * FROM history
        ORDER BY createTime DESC
        """
    )
    abstract fun getAll(): Flow<List<History>>

    @Query(
        """
        SELECT * FROM history
        ORDER BY createTime DESC
        """
    )
    abstract fun getByPage(): PagingSource<Int, History>

    @Query(
        """
        DELETE FROM history
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}