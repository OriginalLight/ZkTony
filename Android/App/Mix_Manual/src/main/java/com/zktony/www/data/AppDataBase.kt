package com.zktony.www.data

import androidx.room.*
import com.zktony.www.data.dao.*
import com.zktony.www.data.entities.*

/**
 * @author 刘贺贺
 */
@Database(
    entities =
    [
        Motor::class,
        Calibration::class,
        CalibrationData::class,
        Cache::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motorDao(): MotorDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun calibrationDataDao(): CalibrationDataDao

    abstract fun cacheDao(): CacheDao
}