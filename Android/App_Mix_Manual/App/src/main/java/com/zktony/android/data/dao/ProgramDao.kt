package com.zktony.android.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zktony.android.data.entities.Program
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class ProgramDao : BaseDao<Program> {
    @Query(
        """
        SELECT * FROM program
        ORDER BY createTime DESC
        """
    )
    abstract fun getAll(): Flow<List<Program>>

    @Query(
        """
        SELECT * FROM program
        ORDER BY id ASC
        """
    )
    abstract fun getByPage(): PagingSource<Int, Program>

    @Query(
        """
        SELECT * FROM program
        WHERE id = :id
        """
    )
    abstract fun getById(id: Long): Flow<Program?>

    @Query(
        """
        DELETE FROM program
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)


    @Query(
        """
        DELETE FROM program
        WHERE id != 1 AND id != 2 AND id != 3
        """
    )
    abstract suspend fun deleteByAll()

    @Query(
        """
        SELECT COUNT(*) FROM ExperimentRecord
        """
    )
    abstract suspend fun count():Int
}