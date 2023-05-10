package com.zktony.android.data.dao

import androidx.room.*
import com.zktony.android.data.entity.Calibration
import com.zktony.room.dao.BaseDao
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 11:09
 */
@Dao
interface CalibrationDao : BaseDao<Calibration> {
    @Query(
        """
        SELECT * FROM calibration
        ORDER BY createTime ASC
        """
    )
    fun getAll(): Flow<List<Calibration>>

    @Query(
        """
        SELECT * FROM calibration
        WHERE id = :id
        """
    )
    fun getById(id: Long): Flow<Calibration>

    @Query(
        """
        DELETE FROM calibration
        WHERE id = :id
        """
    )
    suspend fun deleteById(id: Long)
}