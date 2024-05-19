package com.zktony.room.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zktony.room.entities.Program
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class ProgramDao : BaseDao<Program> {
    @Query(
        """
        SELECT * FROM program
        ORDER BY createTime DESC
        """
    )
    abstract fun getAll(): Flow<List<Program>>

    @Query(
        """
        SELECT * FROM program
        ORDER BY createTime DESC
        """
    )
    abstract fun getByPage(): PagingSource<Int, Program>

    @Query(
        """
        DELETE FROM program
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}