package com.zktony.www.data

import androidx.room.*
import com.zktony.www.data.dao.*
import com.zktony.www.data.entities.*

/**
 * @author 刘贺贺
 */
@Database(
    entities = [Log::class, Program::class, Action::class, Motor::class, Container::class, Calibration::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
    abstract fun programDao(): ProgramDao
    abstract fun actionDao(): ActionDao
    abstract fun motorDao(): MotorDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun containerDao(): ContainerDao
}