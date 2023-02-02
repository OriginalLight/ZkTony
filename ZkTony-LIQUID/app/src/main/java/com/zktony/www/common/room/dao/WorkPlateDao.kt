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

    @Update
    suspend fun update(workPlate: WorkPlate)

    @Query("SELECT * FROM work_plate WHERE workId = :id")
    fun getByWorkId(id: String): Flow<List<WorkPlate>>

    @Query("DELETE FROM work_plate WHERE workId = :id")
    fun deleteByWorkId(id: String)

}