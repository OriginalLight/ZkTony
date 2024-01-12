package com.zktony.android.data

import androidx.room.*
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.ExperimentRecordDao
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.Calibration
import com.zktony.android.data.entities.ExperimentRecord
import com.zktony.android.data.entities.Motor
import com.zktony.android.data.entities.Program

/**
 * @author 刘贺贺
 */
@Database(
    entities =
    [
        Motor::class,
        Calibration::class,
        Program::class,
        ExperimentRecord::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun MotorDao(): MotorDao
    abstract fun CalibrationDao(): CalibrationDao
    abstract fun ProgramDao(): ProgramDao

    abstract fun ExperimentRecordDao(): ExperimentRecordDao
}