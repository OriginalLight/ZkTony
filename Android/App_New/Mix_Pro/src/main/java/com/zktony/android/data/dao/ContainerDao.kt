package com.zktony.android.data.dao

import androidx.room.*
import com.zktony.android.data.entity.Container
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface ContainerDao : BaseDao<Container> {
    @Query(
        """
        SELECT * FROM container
        WHERE id = :id
        LIMIT 1
        """
    )
    fun getById(id: Long): Flow<Container>

    @Query(
        """
        SELECT * FROM container
        ORDER BY createTime ASC
        """
    )
    fun getAll(): Flow<List<Container>>

    @Query(
        """
        DELETE FROM container
        WHERE id = :id
        """
    )
    suspend fun deleteById(id: Long)
}