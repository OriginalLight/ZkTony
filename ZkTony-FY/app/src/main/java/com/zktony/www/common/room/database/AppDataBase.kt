package com.zktony.www.common.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.www.common.room.converters.DateConverters
import com.zktony.www.common.room.dao.*
import com.zktony.www.common.room.entity.*

/**
 * @author 刘贺贺
 */
@Database(
    entities = [LogRecord::class, LogData::class, Program::class, Action::class, Motor::class, Calibration::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logRecordDao(): LogRecordDao
    abstract fun programDao(): ProgramDao
    abstract fun logDataDao(): LogDataDao
    abstract fun actionDao(): ActionDao
    abstract fun motorDao(): MotorDao
    abstract fun calibrationDao(): CalibrationDao
}