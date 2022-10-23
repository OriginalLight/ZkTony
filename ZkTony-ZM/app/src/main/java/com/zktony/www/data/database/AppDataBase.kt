package com.zktony.www.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.www.data.converters.DateConverters
import com.zktony.www.data.dao.LogDataDao
import com.zktony.www.data.dao.LogRecordDao
import com.zktony.www.data.dao.ProgramDao
import com.zktony.www.data.entity.LogData
import com.zktony.www.data.entity.LogRecord
import com.zktony.www.data.entity.Program

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