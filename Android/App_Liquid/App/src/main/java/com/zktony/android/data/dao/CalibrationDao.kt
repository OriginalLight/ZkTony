package com.zktony.android.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zktony.android.data.entities.Calibration
import kotlinx.coroutines.flow.Flow

/**
 * @author 刘贺贺
 * @date 2023/8/30 10:56
 */
@Dao
abstract class CalibrationDao : BaseDao<Calibration> {
    @Query(
        """
        SELECT * FROM calibration
        ORDER BY `index` ASC
        """
    )
    abstract fun getAll(): Flow<List<Calibration>>

    @Query(
        """
        SELECT * FROM calibration
        ORDER BY `index` ASC
        """
    )
    abstract fun getByPage(): PagingSource<Int, Calibration>

    @Query(
        """
        DELETE FROM calibration
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}