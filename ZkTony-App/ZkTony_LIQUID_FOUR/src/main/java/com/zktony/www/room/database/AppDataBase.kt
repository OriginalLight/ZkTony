package com.zktony.www.room.database

import androidx.room.*
import com.zktony.room.converters.DateConverters
import com.zktony.www.room.dao.*
import com.zktony.www.room.entity.*

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
        Container::class,
        Program::class,
        Point::class,
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
    abstract fun programDao(): ProgramDao
    abstract fun containerDao(): ContainerDao
    abstract fun pointDao(): PointDao
}