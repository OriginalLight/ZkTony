package com.zktony.www.room.dao

import androidx.room.*
import com.zktony.common.room.dao.BaseDao
import com.zktony.www.room.entity.Container
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 9:21
 */
@Dao
interface ContainerDao : BaseDao<Container> {
    @Query(
        """
        SELECT * FROM container
        """
    )
    fun getAll(): Flow<List<Container>>

}