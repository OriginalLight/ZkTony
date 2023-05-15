package com.zktony.android.data.dao

import androidx.room.*
import com.zktony.android.data.entity.ProgramEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class ProgramDao : BaseDao<ProgramEntity> {
    @Query(
        """
        SELECT * FROM programs
        """
    )
    abstract fun getAll(): Flow<List<ProgramEntity>>

    @Query(
        """
        SELECT * FROM programs
        WHERE id = :id
        Limit 1
        """
    )
    abstract fun getById(id: Long): Flow<ProgramEntity>

    @Query(
        """
        DELETE FROM programs
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}