package com.zktony.www.common.room.dao

import androidx.room.*
import com.zktony.www.common.room.entity.CalibrationData
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 13:34
 */
@Dao
interface CalibrationDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: CalibrationData)

    @Delete
    suspend fun delete(data: CalibrationData)

    @Query("DELETE FROM calibration_data WHERE calibrationId = :calibrationId")
    suspend fun deleteByCalibrationId(calibrationId: String)

    @Query("SELECT * FROM calibration_data WHERE calibrationId = :calibrationId ORDER BY pumpId ASC")
    fun getByCalibrationId(calibrationId: String): Flow<List<CalibrationData>>

}