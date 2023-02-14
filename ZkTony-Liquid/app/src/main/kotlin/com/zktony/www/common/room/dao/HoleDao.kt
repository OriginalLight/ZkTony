package com.zktony.www.common.room.dao

import androidx.room.*
import com.zktony.www.common.room.entity.Hole
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface HoleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hole: Hole)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(holes: List<Hole>)

    @Query("SELECT * FROM hole WHERE plateId = :plateId")
    fun getByPlateId(plateId: String): Flow<List<Hole>>

    @Query("DELETE FROM hole WHERE plateId = :plateId")
    suspend fun deleteByPlateId(plateId: String)

    @Query("DELETE FROM hole WHERE workId = :workId")
    suspend fun deleteByWorkId(workId: String)

    @Query("SELECT * FROM hole WHERE workId = :workId")
    fun getByWorkId(workId: String): Flow<List<Hole>>

    @Update
    suspend fun update(hole: Hole)

    @Update
    suspend fun updateBatch(holes: List<Hole>)

}