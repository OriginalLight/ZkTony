package com.zktony.www.data.local.dao

import androidx.room.*
import com.zktony.common.room.dao.BaseDao
import com.zktony.www.data.local.entity.CalibrationData
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 13:34
 */
@Dao
interface CalibrationDataDao : BaseDao<CalibrationData> {
    @Query(
        """
        DELETE FROM calibration_data
        WHERE calibrationId = :id
        """
    )
    suspend fun deleteBySubId(id: String)

    @Query(
        """
        SELECT * FROM calibration_data
        WHERE calibrationId = :id
        ORDER BY pumpId ASC , createTime ASC
        """
    )
    fun getBySubId(id: String): Flow<List<CalibrationData>>
}