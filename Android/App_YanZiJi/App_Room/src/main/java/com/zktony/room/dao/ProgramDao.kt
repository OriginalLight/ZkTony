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
        WHERE id = :id
        """
    )
    abstract fun getById(id: Long): Program?

    @Query(
        """
        SELECT * FROM programs
        WHERE (CASE WHEN :name IS NULL THEN 1 ELSE name LIKE '%' || :name || '%' END)
        AND (CASE WHEN :startTime IS NULL THEN 1 ELSE createTime >= :startTime END)
        AND (CASE WHEN :endTime IS NULL THEN 1 ELSE createTime <= :endTime END)
        ORDER BY createTime DESC
        """
    )
    abstract fun getByPage(
        name: String?,
        startTime: Long?,
        endTime: Long?
    ): PagingSource<Int, Program>


    @Query(
        """
        SELECT * FROM programs
        WHERE name = :name
        """
    )
    abstract suspend fun getByName(name: String): List<Program>

    @Query(
        """
        DELETE FROM programs
        WHERE id IN (:ids)
        """
    )
    abstract suspend fun deleteByIds(ids: List<Long>): Int
}