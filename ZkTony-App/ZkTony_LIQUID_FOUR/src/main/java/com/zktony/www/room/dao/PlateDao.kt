package com.zktony.www.room.dao

import androidx.room.*
import com.zktony.common.room.dao.BaseDao
import com.zktony.www.room.entity.Plate
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
        ORDER BY `index` ASC
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