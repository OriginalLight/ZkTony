package com.zktony.www.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.common.room.converters.DateConverters
import com.zktony.www.room.dao.*
import com.zktony.www.room.entity.*

/**
 * @author 刘贺贺
 */
@Database(
    entities = [Log::class, Program::class, Action::class, Motor::class, Container::class, Calibration::class, CalibrationData::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
    abstract fun programDao(): ProgramDao
    abstract fun actionDao(): ActionDao
    abstract fun motorDao(): MotorDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun containerDao(): ContainerDao
    abstract fun calibrationDataDao(): CalibrationDataDao
}