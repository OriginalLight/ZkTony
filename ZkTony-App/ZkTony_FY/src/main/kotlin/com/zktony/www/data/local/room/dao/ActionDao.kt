package com.zktony.www.data.local.room.dao

import androidx.room.*
import com.zktony.www.data.local.room.entity.Action
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
        WHERE programId = :programId
        """
    )
    suspend fun deleteByProgramId(programId: String)

    @Query(
        """
        SELECT * FROM `action`
        WHERE programId = :programId
        ORDER BY `order` ASC
        """
    )
    fun getByProgramId(programId: String): Flow<List<Action>>
}