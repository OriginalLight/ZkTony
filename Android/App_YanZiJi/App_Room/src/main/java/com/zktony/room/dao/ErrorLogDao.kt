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
        SELECT * FROM error_logs
        WHERE createTime < :expired
        """
    )
    abstract fun getBeforeTime(expired: Long): List<ErrorLog>
}