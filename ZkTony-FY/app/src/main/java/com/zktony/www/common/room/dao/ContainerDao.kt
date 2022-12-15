package com.zktony.www.common.room.dao

import androidx.room.*
import com.zktony.www.common.room.entity.Container
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 9:21
 */
@Dao
interface ContainerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(container: Container)

    @Query("SELECT * FROM container")
    fun getAll(): Flow<List<Container>>

}