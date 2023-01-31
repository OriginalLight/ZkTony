package com.zktony.www.common.room.dao

import androidx.room.*
import com.zktony.www.common.room.entity.Calibration
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 11:09
 */
@Dao
interface CalibrationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calibration: Calibration)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(calibration: Calibration)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBatch(calibrations: List<Calibration>)

    @Delete
    suspend fun delete(calibration: Calibration)

    @Query("SELECT * FROM calibration")
    fun getAll(): Flow<List<Calibration>>
}