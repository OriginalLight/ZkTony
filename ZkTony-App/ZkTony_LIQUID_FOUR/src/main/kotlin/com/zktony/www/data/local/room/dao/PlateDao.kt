package com.zktony.www.data.local.room.dao

import androidx.room.*
import com.zktony.www.data.local.room.entity.Plate
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface PlateDao : BaseDao<Plate> {
    @Query(
        """
        SELECT * FROM plate
        WHERE id = :id
        LIMIT 1
        """
    )
    fun getById(id: Long): Flow<Plate>

    @Query(
        """
        SELECT * FROM plate
        """
    )
    fun getAll(): Flow<List<Plate>>

    @Query(
        """
        SELECT * FROM plate
        WHERE subId = :id
        """
    )
    fun getBySubId(id: Long): Flow<List<Plate>>


    @Query(
        """
        SELECT * FROM plate
        WHERE id IN (:idList)
        """
    )
    fun getByIdList(idList: List<Long>): Flow<List<Plate>>
}