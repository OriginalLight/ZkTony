package com.zktony.android.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zktony.android.data.entities.ErrorRecord
import com.zktony.android.data.entities.ExperimentRecord
import com.zktony.android.data.entities.SportsLog
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class SportsLogDao : BaseDao<SportsLog> {
    @Query(
        """
        SELECT * FROM sportslog
        ORDER BY createTime DESC
        """
    )
    abstract fun getAll(): Flow<List<SportsLog>>


    @Query(
        """
        SELECT * FROM sportslog
        ORDER BY createTime DESC
        """
    )
    abstract fun getAll1(): List<SportsLog>

    @Query(
        """
        SELECT * FROM sportslog
        ORDER BY id DESC
        """
    )
    abstract fun getByPage(): PagingSource<Int, SportsLog>

    @Query(
        """
        SELECT * FROM sportslog
        GROUP BY logName
        ORDER BY id DESC
        """
    )
    abstract fun getByPageDis(): PagingSource<Int, SportsLog>

    @Query(
        """
        SELECT * FROM sportslog
        WHERE id = :id
        """
    )
    abstract fun getById(id: Long): Flow<SportsLog?>

    @Query(
        """
        DELETE FROM sportslog
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)

    @Query(
        """
        DELETE FROM sportslog
        WHERE createTime <= :createTime
        """
    )
    abstract suspend fun deleteByDate(createTime: Date)


}