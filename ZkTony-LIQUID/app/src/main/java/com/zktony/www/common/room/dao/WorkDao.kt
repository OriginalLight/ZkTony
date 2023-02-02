package com.zktony.www.common.room.dao

import androidx.room.*
import com.zktony.www.common.room.entity.Work
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface WorkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(work: Work)

    @Delete
    suspend fun delete(work: Work)

    @Update
    suspend fun update(work: Work)

    @Query("SELECT * FROM work")
    fun getAll(): Flow<List<Work>>

    @Query("SELECT * FROM work WHERE id = :id Limit 1")
    fun getById(id: String): Flow<Work>

}