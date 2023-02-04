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

    @Delete
    suspend fun delete(calibration: Calibration)

    @Query("SELECT * FROM calibration")
    fun getAll(): Flow<List<Calibration>>

    @Query("SELECT * FROM calibration WHERE name = :name")
    fun getByName(name: String): Flow<List<Calibration>>

    @Query("SELECT * FROM calibration WHERE status = 1")
    fun getDefault(): Flow<List<Calibration>>

    @Query("SELECT * FROM calibration WHERE id = :id")
    fun getById(id: String): Flow<Calibration>

    @Query("UPDATE calibration SET status = 0")
    suspend fun removeDefault()
}