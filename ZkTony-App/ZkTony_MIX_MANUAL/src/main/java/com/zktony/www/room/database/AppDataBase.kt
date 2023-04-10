package com.zktony.www.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.room.converters.DateConverters
import com.zktony.www.room.dao.CalibrationDao
import com.zktony.www.room.dao.CalibrationDataDao
import com.zktony.www.room.dao.MotorDao
import com.zktony.www.room.entity.Calibration
import com.zktony.www.room.entity.CalibrationData
import com.zktony.www.room.entity.Motor

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