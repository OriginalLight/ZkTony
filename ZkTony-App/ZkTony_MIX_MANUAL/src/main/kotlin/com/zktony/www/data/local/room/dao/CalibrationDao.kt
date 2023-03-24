package com.zktony.www.data.local.room.dao

import androidx.room.*
import com.zktony.www.data.local.room.entity.Calibration
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
}