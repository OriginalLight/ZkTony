package com.zktony.android.data.dao

import androidx.room.*
import com.zktony.android.data.entities.Motor
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class MotorDao : BaseDao<Motor> {
    @Query(
        """
        SELECT * FROM motors
        """
    )
    abstract fun getAll(): Flow<List<Motor>>
}