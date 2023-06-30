package com.zktony.android.data

import androidx.room.*
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.CalibrationEntity
import com.zktony.android.data.entities.MotorEntity
import com.zktony.android.data.entities.ProgramEntity

/**
 * @author 刘贺贺
 */
@Database(
    entities =
    [
        MotorEntity::class,
        CalibrationEntity::class,
        ProgramEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motorDao(): MotorDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun programDao(): ProgramDao
}