package com.zktony.android.data.dao

import androidx.room.*
import com.zktony.android.data.model.Program
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
        ORDER BY create_time DESC
        """
    )
    abstract fun getAll(): Flow<List<Program>>

    @Query(
        """
        DELETE FROM programs
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}