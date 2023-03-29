package com.zktony.www.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.common.room.converters.DateConverters
import com.zktony.www.data.local.dao.CalibrationDao
import com.zktony.www.data.local.dao.CalibrationDataDao
import com.zktony.www.data.local.dao.MotorDao
import com.zktony.www.data.local.entity.Calibration
import com.zktony.www.data.local.entity.CalibrationData
import com.zktony.www.data.local.entity.Motor

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