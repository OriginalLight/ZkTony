package com.zktony.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.zktony.room.entities.LogSnapshot

@Dao
abstract class LogSnapshotDao : BaseDao<LogSnapshot> {

    @Query("""
        SELECT * FROM log_snapshots
        WHERE subId < 2000
    """)
    abstract fun test() :List<LogSnapshot>
}