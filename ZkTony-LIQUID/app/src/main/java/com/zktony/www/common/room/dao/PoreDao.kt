package com.zktony.www.common.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zktony.www.common.room.entity.Pore
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface PoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pore: Pore)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(pores: List<Pore>)

    @Query("SELECT * FROM pore WHERE plateId = :plateId")
    fun getPlatePore(plateId: String): Flow<List<Pore>>

    @Query("DELETE FROM pore WHERE plateId = :plateId")
    suspend fun deleteByPlateId(plateId: String)

}