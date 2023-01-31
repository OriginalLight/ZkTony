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
    entities = [Log::class, Program::class, Motor::class, Calibration::class, CalibrationData::class, Plate::class, Pore::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
    abstract fun programDao(): ProgramDao
    abstract fun motorDao(): MotorDao
    abstract fun calibrationDao(): CalibrationDao

    abstract fun calibrationDataDao(): CalibrationDataDao

    abstract fun plateDao(): PlateDao

    abstract fun poreDao(): PoreDao
}