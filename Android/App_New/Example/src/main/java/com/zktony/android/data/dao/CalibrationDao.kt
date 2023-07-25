package com.zktony.android.data.dao

import androidx.room.*
import com.zktony.android.data.entities.CalibrationEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 11:09
 */
@Dao
abstract class CalibrationDao : BaseDao<CalibrationEntity> {
    @Query(
        """
        SELECT * FROM calibrations
        ORDER BY create_time ASC
        """
    )
    abstract fun getAll(): Flow<List<CalibrationEntity>>

    @Query(
        """
        UPDATE calibrations
        SET active = (CASE WHEN id = :id THEN 1 ELSE 0 END)
        """
    )
    abstract suspend fun active(id: Long)

    @Query(
        """
        DELETE FROM calibrations
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}