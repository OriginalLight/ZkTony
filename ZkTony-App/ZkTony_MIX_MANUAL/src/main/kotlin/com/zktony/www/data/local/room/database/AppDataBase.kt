package com.zktony.www.data.local.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.www.data.local.room.converters.DateConverters
import com.zktony.www.data.local.room.dao.*
import com.zktony.www.data.local.room.entity.*

/**
 * @author 刘贺贺
 */
@Database(
    entities =
    [
        Motor::class,
        Calibration::class,
        CalibrationData::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motorDao(): MotorDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun calibrationDataDao(): CalibrationDataDao
}