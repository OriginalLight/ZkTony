package com.zktony.www.data

import androidx.room.*
import com.zktony.www.data.dao.*
import com.zktony.www.data.entities.*

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