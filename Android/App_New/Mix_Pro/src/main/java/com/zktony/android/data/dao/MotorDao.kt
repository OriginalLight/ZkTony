package com.zktony.android.data.dao

import androidx.room.*
import com.zktony.room.dao.BaseDao
import com.zktony.android.data.entity.Motor
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
interface MotorDao : BaseDao<Motor> {
    @Query(
        """
        SELECT * FROM motor
        """
    )
    fun getAll(): Flow<List<Motor>>
}