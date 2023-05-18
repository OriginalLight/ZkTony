package com.zktony.www.data.dao

import androidx.room.*
import com.zktony.www.data.entities.Container
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
        ORDER BY type ASC
        """
    )
    fun getAll(): Flow<List<Container>>


    @Query(
        """
        SELECT * FROM container
        WHERE type = :type
        """
    )
    fun getByType(type: Int): Flow<List<Container>>
}