package com.zktony.www.data.local.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.www.data.local.room.converters.DateConverters
import com.zktony.www.data.local.room.dao.LogDataDao
import com.zktony.www.data.local.room.dao.LogRecordDao
import com.zktony.www.data.local.room.dao.ProgramDao
import com.zktony.www.data.local.room.entity.LogData
import com.zktony.www.data.local.room.entity.LogRecord
import com.zktony.www.data.local.room.entity.Program

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