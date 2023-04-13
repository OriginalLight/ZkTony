package com.zktony.www.room.dao

import androidx.room.*
import com.zktony.room.dao.BaseDao
import com.zktony.www.room.entity.Point
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface PointDao : BaseDao<Point> {
    @Query(
        """
        SELECT * FROM point
        WHERE subId = :id
        """
    )
    fun getBySubId(id: Long): Flow<List<Point>>


    @Query(
        """
        DELETE FROM point
        WHERE subId = :id
        """
    )
    suspend fun deleteBySubId(id: Long)

    @Query(
        """
        SELECT * FROM point
        WHERE subId = :id
        AND `index` = :index
        """
    )
    fun getBySudIdByIndex(id: Long, index: Int): Flow<List<Point>>

}