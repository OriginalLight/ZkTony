package com.zktony.www.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.common.room.converters.DateConverters
import com.zktony.www.data.local.dao.LogDataDao
import com.zktony.www.data.local.dao.LogRecordDao
import com.zktony.www.data.local.dao.ProgramDao
import com.zktony.www.data.local.entity.LogData
import com.zktony.www.data.local.entity.LogRecord
import com.zktony.www.data.local.entity.Program

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