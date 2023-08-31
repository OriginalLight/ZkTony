package com.zktony.android.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zktony.android.data.entities.Curve
import kotlinx.coroutines.flow.Flow

/**
 * @author 刘贺贺
 * @date 2023/8/30 10:56
 */
@Dao
abstract class CurveDao : BaseDao<Curve> {
    @Query(
        """
        SELECT * FROM curve
        ORDER BY `index` ASC
        """
    )
    abstract fun getAll(): Flow<List<Curve>>

    @Query(
        """
        SELECT * FROM curve
        ORDER BY `index` ASC
        """
    )
    abstract fun getByPage(): PagingSource<Int, Curve>

    @Query(
        """
        DELETE FROM curve
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}