package com.zktony.android.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zktony.android.data.entities.Motor
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class MotorDao : BaseDao<Motor> {
    @Query(
        """
        SELECT * FROM motor
        """
    )
    abstract fun getAll(): Flow<List<Motor>>

    @Query(
        """
        SELECT * FROM motor
        ORDER BY createTime DESC
        """
    )
    abstract fun getByPage(): PagingSource<Int, Motor>

    @Query(
        """
        DELETE FROM motor
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}