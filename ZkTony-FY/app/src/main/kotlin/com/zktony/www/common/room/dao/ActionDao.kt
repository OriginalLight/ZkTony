package com.zktony.www.common.room.dao

import androidx.room.*
import com.zktony.www.common.room.entity.Action
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-09-30 11:25
 */
@Dao
interface ActionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: Action)

    @Delete
    suspend fun delete(action: Action)

    @Query("DELETE FROM `action` WHERE programId = :programId")
    suspend fun deleteByProgramId(programId: String)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(action: Action)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBatch(actions: List<Action>)

    @Query("SELECT * FROM `action` WHERE programId = :programId ORDER BY `order` ASC")
    fun getByProgramId(programId: String): Flow<List<Action>>
}