package com.zktony.android.data

import androidx.room.*
import com.zktony.android.data.dao.*
import com.zktony.android.data.entities.*

/**
 * @author 刘贺贺
 */
@Database(
    entities =
    [Calibration::class, Motor::class, History::class, Program::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun CalibrationDao(): CalibrationDao
    abstract fun MotorDao(): MotorDao
    abstract fun HistoryDao(): HistoryDao
    abstract fun ProgramDao(): ProgramDao
}