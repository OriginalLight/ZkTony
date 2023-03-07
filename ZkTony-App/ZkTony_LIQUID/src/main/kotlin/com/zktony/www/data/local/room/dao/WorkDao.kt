package com.zktony.www.data.local.room.dao

import androidx.room.*
import com.zktony.www.data.local.room.entity.Work
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface WorkDao : BaseDao<Work> {
    @Query("SELECT * FROM work")
    fun getAll(): Flow<List<Work>>

    @Query("SELECT * FROM work WHERE id = :id Limit 1")
    fun getById(id: String): Flow<Work>

}