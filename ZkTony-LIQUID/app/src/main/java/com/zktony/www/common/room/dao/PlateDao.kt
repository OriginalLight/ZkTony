package com.zktony.www.common.room.dao

import androidx.room.*
import com.zktony.www.common.room.entity.Plate
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface PlateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plate: Plate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(plates: List<Plate>)

    @Update
    suspend fun update(plate: Plate)

    @Query("SELECT * FROM plate WHERE sort = :id LIMIT 1")
    fun getPlateBySort(id: Int): Flow<Plate>

    @Query("SELECT * FROM plate")
    fun getAllPlate(): Flow<List<Plate>>
}