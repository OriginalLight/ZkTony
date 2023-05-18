package com.zktony.www.data.dao

import androidx.room.*
import com.zktony.www.data.entities.Cache
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 11:09
 */
@Dao
interface CacheDao : BaseDao<Cache> {
    @Query(
        """
        SELECT * FROM cache
        """
    )
    fun getAll(): Flow<List<Cache>>
}