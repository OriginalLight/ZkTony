package com.zktony.www.data.dao

import androidx.room.*
import com.zktony.www.data.model.Container
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