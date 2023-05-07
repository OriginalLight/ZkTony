package com.zktony.android.data.database

import androidx.room.*
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.CalibrationDataDao
import com.zktony.android.data.dao.ContainerDao
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.dao.PointDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entity.Calibration
import com.zktony.android.data.entity.CalibrationData
import com.zktony.android.data.entity.Container
import com.zktony.android.data.entity.Motor
import com.zktony.android.data.entity.Point
import com.zktony.android.data.entity.Program
import com.zktony.room.converters.DateConverters

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
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motorDao(): MotorDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun calibrationDataDao(): CalibrationDataDao
    abstract fun programDao(): ProgramDao
    abstract fun containerDao(): ContainerDao
    abstract fun pointDao(): PointDao
}