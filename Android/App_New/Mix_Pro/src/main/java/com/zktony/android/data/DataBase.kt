package com.zktony.android.data

import androidx.room.*
import com.zktony.android.data.dao.*
import com.zktony.android.data.entity.*

/**
 * @author 刘贺贺
 */
@Database(
    entities =
    [
        Motor::class,
        Calibration::class,
        Program::class,
        Container::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motorDao(): MotorDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun programDao(): ProgramDao
    abstract fun containerDao(): ContainerDao
}