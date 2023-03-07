package com.zktony.www.data.local.room.dao

import androidx.room.*
import com.zktony.www.data.local.room.entity.Motor
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface MotorDao : BaseDao<Motor> {
    @Query("SELECT * FROM motor")
    fun getAll(): Flow<List<Motor>>

    @Query("SELECT * FROM motor WHERE id = :id")
    fun getById(id: Int): Flow<Motor>
}