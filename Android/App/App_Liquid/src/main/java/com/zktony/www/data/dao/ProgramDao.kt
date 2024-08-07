package com.zktony.www.data.dao

import androidx.room.*
import com.zktony.www.data.entities.Program
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface ProgramDao : BaseDao<Program> {
    @Query(
        """
        SELECT * FROM program
        """
    )
    fun getAll(): Flow<List<Program>>

    @Query(
        """
        SELECT * FROM program
        WHERE id = :id
        Limit 1
        """
    )
    fun getById(id: Long): Flow<Program>

}