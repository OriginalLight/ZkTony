package com.zktony.android.data.dao

import androidx.room.*
import com.zktony.room.dao.BaseDao
import com.zktony.android.data.entity.CalibrationData
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 11:09
 */
@Dao
interface CalibrationDataDao : BaseDao<CalibrationData> {

    @Query(
        """
        DELETE FROM calibration_data
        WHERE subId = :id
        """
    )
    suspend fun deleteBySubId(id: Long)

    @Query(
        """
        SELECT * FROM calibration_data
        WHERE subId = :id
        ORDER BY pumpId ASC , createTime ASC
        """
    )
    fun getBySubId(id: Long): Flow<List<CalibrationData>>

}