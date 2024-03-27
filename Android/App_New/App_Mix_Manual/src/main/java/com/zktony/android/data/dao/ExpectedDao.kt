package com.zktony.android.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zktony.android.data.entities.Expected
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class ExpectedDao : BaseDao<Expected> {

    @Query(
        """
        SELECT * FROM expected
        """
    )
    abstract fun getAll(): Flow<List<Expected>>

    @Query(
        """
        SELECT * FROM expected
        ORDER BY id DESC
        """
    )
    abstract fun getByPage(): PagingSource<Int, Expected>

    @Query(
        """
        SELECT * FROM expected
        WHERE id = :id
        """
    )
    abstract fun getById(id: Long): Flow<Expected?>

    @Query(
        """
        DELETE FROM expected
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}