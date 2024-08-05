package com.zktony.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.zktony.room.entities.Log
import kotlinx.coroutines.flow.Flow

/**
 * @author 刘贺贺
 * @date 2023/8/30 10:56
 */
@Dao
abstract class LogDao : BaseDao<Log> {
    @Query(
        """
        SELECT * FROM logs
        ORDER BY createTime DESC
        """
    )
    abstract fun getAll(): Flow<List<Log>>

    @Query(
        """
        SELECT * FROM logs
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
    ): PagingSource<Int, Log>

    @Query(
        """
        DELETE FROM logs
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT * FROM logs
        WHERE createTime < :expired
        """
    )
    abstract fun getBeforeTime(expired: Long): List<Log>

    @Query(
        """
        DELETE FROM logs
        WHERE id IN (:ids)
        """
    )
    abstract suspend fun deleteByIds(ids: List<Long>): Int
}