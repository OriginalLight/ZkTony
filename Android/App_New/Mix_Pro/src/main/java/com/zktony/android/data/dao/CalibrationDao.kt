package com.zktony.android.data.dao

import androidx.room.*
import com.zktony.android.data.entity.Calibration
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 11:09
 */
@Dao
interface CalibrationDao : BaseDao<Calibration> {
    @Query(
        """
        SELECT * FROM calibration
        ORDER BY createTime ASC
        """
    )
    fun getAll(): Flow<List<Calibration>>

    @Query(
        """
        SELECT * FROM calibration
        WHERE id = :id
        """
    )
    fun getById(id: Long): Flow<Calibration>

    // 符合id的active更新为1，其他的更新为0
    @Query(
        """
        UPDATE calibration
        SET active = (CASE WHEN id = :id THEN 1 ELSE 0 END)
        """
    )
    suspend fun active(id: Long)
}