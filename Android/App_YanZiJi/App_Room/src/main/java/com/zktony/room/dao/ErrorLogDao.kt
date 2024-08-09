package com.zktony.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.zktony.room.entities.ErrorLog
import com.zktony.room.entities.Log
import com.zktony.room.entities.Program
import com.zktony.room.repository.ErrorLogRepository

@Dao
abstract class ErrorLogDao : BaseDao<ErrorLog> {

    @Query(
        """
        SELECT * FROM error_logs
        ORDER BY createTime DESC
        """
    )
    abstract fun getByPage(): PagingSource<Int, ErrorLog>

    @Query(
        """
        DELETE FROM error_logs
        WHERE id NOT IN (
            SELECT id FROM error_logs ORDER BY createTime DESC LIMIT :keep
        )
        """
    )
    abstract suspend fun deleteOutOf(keep: Int) : Int
}