package com.zktony.android.data.dao

import androidx.room.*
import com.zktony.android.data.entity.CalibrationData
import com.zktony.room.dao.BaseDao
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
        WHERE id = :id
        """
    )
    suspend fun deleteById(id: Long)

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
        ORDER BY `index` ASC , createTime ASC
        """
    )
    fun getBySubId(id: Long): Flow<List<CalibrationData>>

    @Query(
        """
        DELETE FROM calibration_data
        WHERE subId = :id
        AND `index` = :index
        """
    )
    suspend fun deleteByIndex(id: Long, index: Int)

}