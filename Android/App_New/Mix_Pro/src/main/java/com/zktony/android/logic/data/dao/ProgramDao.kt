package com.zktony.android.logic.data.dao

import androidx.room.*
import com.zktony.android.logic.data.entities.PWC
import com.zktony.android.logic.data.entities.ProgramEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class ProgramDao : BaseDao<ProgramEntity> {
    @Query(
        """
        SELECT * FROM programs
        """
    )
    @Transaction
    abstract fun getAll(): Flow<List<PWC>>

    @Query(
        """
        DELETE FROM programs
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}