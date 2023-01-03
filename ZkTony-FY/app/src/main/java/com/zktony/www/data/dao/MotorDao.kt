package com.zktony.www.data.dao

import androidx.room.*
import com.zktony.www.data.model.Motor
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface MotorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(motor: Motor)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(motors: List<Motor>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(motor: Motor)

    @Query("SELECT * FROM motor ORDER BY id ASC")
    fun getAll(): Flow<List<Motor>>

    @Query("SELECT * FROM motor WHERE id = :id LIMIT 1")
    fun getById(id: Int): Flow<Motor>
}