package com.zktony.www.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zktony.www.data.entity.Calibration
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

    @Query("SELECT * FROM calibration")
    fun getCailbration(): Flow<List<Calibration>>
}