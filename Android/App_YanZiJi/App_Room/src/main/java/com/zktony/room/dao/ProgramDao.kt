package com.zktony.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
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
        SELECT * FROM programs
        ORDER BY createTime DESC
        """
    )
    abstract fun getAll(): Flow<List<Program>>

    @Query(
        """
        SELECT * FROM programs
        ORDER BY createTime DESC
        """
    )
    abstract fun getByPage(): PagingSource<Int, Program>

    @Query(
        """
        DELETE FROM programs
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT * FROM programs
        WHERE name = :name
        """
    )
    abstract fun getByName(name: String): Program?
}