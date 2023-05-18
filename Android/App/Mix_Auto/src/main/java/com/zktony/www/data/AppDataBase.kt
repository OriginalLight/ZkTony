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
        Program::class,
        Container::class,
        Point::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(com.zktony.www.data.DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motorDao(): MotorDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun calibrationDataDao(): CalibrationDataDao
    abstract fun programDao(): ProgramDao
    abstract fun containerDao(): ContainerDao
    abstract fun pointDao(): PointDao
}