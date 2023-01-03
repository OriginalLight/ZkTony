package com.zktony.www.data.dao

import androidx.room.*
import com.zktony.www.data.model.CalibrationData
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 13:34
 */
@Dao
interface CalibrationDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calibrationData: CalibrationData)

    @Delete
    suspend fun delete(calibrationData: CalibrationData)

    @Query("DELETE FROM calibrationData WHERE calibrationId = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM CalibrationData WHERE calibrationId = :id ORDER BY motorId ASC")
    fun getByCaliId(id: String): Flow<List<CalibrationData>>

}