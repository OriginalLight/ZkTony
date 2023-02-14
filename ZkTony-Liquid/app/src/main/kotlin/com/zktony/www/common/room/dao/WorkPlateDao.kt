package com.zktony.www.common.room.dao

import androidx.room.*
import com.zktony.www.common.room.entity.WorkPlate
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface WorkPlateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workPlate: WorkPlate)

    @Delete
    suspend fun delete(workPlate: WorkPlate)

    @Query("DELETE FROM work_plate WHERE id = :id")
    suspend fun deleteById(id: String)

    @Update
    suspend fun update(workPlate: WorkPlate)

    @Query("SELECT * FROM work_plate WHERE workId = :id ORDER BY sort ASC")
    fun getByWorkId(id: String): Flow<List<WorkPlate>>

    @Query("DELETE FROM work_plate WHERE workId = :id")
    suspend fun deleteByWorkId(id: String)

    @Query("SELECT * FROM work_plate WHERE id = :id")
    fun getById(id: String): Flow<WorkPlate>

}