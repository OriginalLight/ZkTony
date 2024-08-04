package com.zktony.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.room.dao.ErrorLogDao
import com.zktony.room.dao.LogDao
import com.zktony.room.dao.LogSnapshotDao
import com.zktony.room.dao.ProgramDao
import com.zktony.room.dao.UserDao
import com.zktony.room.entities.ErrorLog
import com.zktony.room.entities.Log
import com.zktony.room.entities.LogSnapshot
import com.zktony.room.entities.Program
import com.zktony.room.entities.User

/**
 * @author 刘贺贺
 */
@Database(
    entities =
    [Program::class, ErrorLog::class, Log::class, LogSnapshot::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ProgramDao(): ProgramDao
    abstract fun errorLogDao(): ErrorLogDao
    abstract fun logDao(): LogDao
    abstract fun logSnapshotDao(): LogSnapshotDao
    abstract fun UserDao(): UserDao
}