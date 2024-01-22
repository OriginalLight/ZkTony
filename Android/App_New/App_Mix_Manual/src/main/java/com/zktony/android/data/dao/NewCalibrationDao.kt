package com.zktony.android.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zktony.android.data.entities.NewCalibration
import com.zktony.android.data.entities.Setting
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class NewCalibrationDao : BaseDao<NewCalibration> {

    @Query(
        """
        SELECT * FROM new_calibration
        ORDER BY id DESC
        """
    )
    abstract fun getByPage(): PagingSource<Int, NewCalibration>

    @Query(
        """
        SELECT * FROM new_calibration
        WHERE id = :id
        """
    )
    abstract fun getById(id: Long): Flow<NewCalibration?>

    @Query(
        """
        DELETE FROM new_calibration
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}