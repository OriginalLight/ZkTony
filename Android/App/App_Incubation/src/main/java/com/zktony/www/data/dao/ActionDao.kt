package com.zktony.www.data.dao

import androidx.room.*
import com.zktony.www.data.entities.Action
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-09-30 11:25
 */
@Dao
interface ActionDao : BaseDao<Action> {
    @Query(
        """
        DELETE FROM `action`
        WHERE subId = :id
        """
    )
    suspend fun deleteBySubId(id: Long)

    @Query(
        """
        SELECT * FROM `action`
        WHERE subId = :id
        ORDER BY `index` ASC
        """
    )
    fun getBySubId(id: Long): Flow<List<Action>>
}