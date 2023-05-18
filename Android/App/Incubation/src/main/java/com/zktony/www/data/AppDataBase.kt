package com.zktony.www.data

import androidx.room.*
import com.zktony.www.data.DateConverters
import com.zktony.www.data.dao.ActionDao
import com.zktony.www.data.dao.CalibrationDao
import com.zktony.www.data.dao.CalibrationDataDao
import com.zktony.www.data.dao.ContainerDao
import com.zktony.www.data.dao.LogDao
import com.zktony.www.data.dao.MotorDao
import com.zktony.www.data.dao.ProgramDao
import com.zktony.www.data.entities.Action
import com.zktony.www.data.entities.Calibration
import com.zktony.www.data.entities.CalibrationData
import com.zktony.www.data.entities.Container
import com.zktony.www.data.entities.Log
import com.zktony.www.data.entities.Motor
import com.zktony.www.data.entities.Program

/**
 * @author 刘贺贺
 */
@Database(
    entities = [Log::class, Program::class, Action::class, Motor::class, Container::class, Calibration::class, CalibrationData::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(com.zktony.www.data.DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
    abstract fun programDao(): ProgramDao
    abstract fun actionDao(): ActionDao
    abstract fun motorDao(): MotorDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun containerDao(): ContainerDao
    abstract fun calibrationDataDao(): CalibrationDataDao
}