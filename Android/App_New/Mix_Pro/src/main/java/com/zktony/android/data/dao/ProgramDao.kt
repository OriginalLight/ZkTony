package com.zktony.android.data.dao

import androidx.room.*
import com.zktony.android.data.entity.Program
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class ProgramDao : BaseDao<Program> {
    @Query(
        """
        SELECT * FROM programs
        """
    )
    abstract fun getAll(): Flow<List<Program>>

    @Query(
        """
        SELECT * FROM programs
        WHERE id = :id
        Limit 1
        """
    )
    abstract fun getById(id: Long): Flow<Program>
}