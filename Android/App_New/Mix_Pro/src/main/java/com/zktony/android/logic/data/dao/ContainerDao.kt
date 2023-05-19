package com.zktony.android.logic.data.dao

import androidx.room.*
import com.zktony.android.logic.data.entities.ContainerEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 */
@Dao
abstract class ContainerDao : BaseDao<ContainerEntity> {
    @Query(
        """
        SELECT * FROM containers
        ORDER BY create_time ASC
        """
    )
    abstract fun getAll(): Flow<List<ContainerEntity>>

    @Query(
        """
        DELETE FROM containers
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}