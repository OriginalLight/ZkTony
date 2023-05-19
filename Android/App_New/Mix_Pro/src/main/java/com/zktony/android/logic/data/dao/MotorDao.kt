package com.zktony.android.logic.data.dao

import androidx.room.*
import com.zktony.android.logic.data.entities.MotorEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class MotorDao : BaseDao<MotorEntity> {
    @Query(
        """
        SELECT * FROM motors
        """
    )
    abstract fun getAll(): Flow<List<MotorEntity>>
}