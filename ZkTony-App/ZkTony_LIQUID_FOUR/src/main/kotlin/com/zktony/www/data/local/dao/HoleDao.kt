package com.zktony.www.data.local.dao

import androidx.room.*
import com.zktony.common.room.dao.BaseDao
import com.zktony.www.data.local.entity.Hole
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface HoleDao : BaseDao<Hole> {
    @Query(
        """
        SELECT * FROM hole
        WHERE subId = :id
        """
    )
    fun getBySubId(id: Long): Flow<List<Hole>>

    @Query(
        """
        SELECT * FROM hole
        WHERE subId IN (:idList)
        """
    )
    fun getBySudIdList(idList: List<Long>): Flow<List<Hole>>

    @Query(
        """
        DELETE FROM hole
        WHERE subId = :id
        """
    )
    suspend fun deleteBySubId(id: Long)
}