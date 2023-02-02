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
    entities =
    [
        Log::class,
        Motor::class,
        Calibration::class,
        CalibrationData::class,
        Plate::class,
        Hole::class,
        Work::class,
        WorkPlate::class,
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

    abstract fun workDao(): WorkDao

    abstract fun workPlateDao(): WorkPlateDao
}