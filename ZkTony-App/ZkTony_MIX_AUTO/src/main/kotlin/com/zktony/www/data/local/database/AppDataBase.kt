package com.zktony.www.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.common.room.converters.DateConverters
import com.zktony.www.data.local.dao.*
import com.zktony.www.data.local.entity.*

/**
 * @author 刘贺贺
 */
@Database(
    entities =
    [
        Log::class,
        Motor::class,
        Calibration::class,
        CalibrationData::class,
        Plate::class,
        Hole::class,
        Program::class,
        Container::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
    abstract fun motorDao(): MotorDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun calibrationDataDao(): CalibrationDataDao
    abstract fun plateDao(): PlateDao
    abstract fun holeDao(): HoleDao
    abstract fun programDao(): ProgramDao
    abstract fun containerDao(): ContainerDao
}