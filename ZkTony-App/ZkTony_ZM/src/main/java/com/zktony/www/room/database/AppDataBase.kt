package com.zktony.www.room.database

import androidx.room.*
import com.zktony.room.converters.DateConverters
import com.zktony.www.room.dao.*
import com.zktony.www.room.entity.*

/**
 * @author 刘贺贺
 */
@Database(
    entities = [LogRecord::class, LogData::class, Program::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logRecordDao(): LogRecordDao
    abstract fun programDao(): ProgramDao
    abstract fun logDataDao(): LogDataDao
}