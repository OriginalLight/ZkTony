package com.zktony.android.logic.data

import androidx.room.*
import com.zktony.android.logic.data.dao.CalibrationDao
import com.zktony.android.logic.data.dao.MotorDao
import com.zktony.android.logic.data.dao.ProgramDao
import com.zktony.android.logic.data.entities.CalibrationEntity
import com.zktony.android.logic.data.entities.MotorEntity
import com.zktony.android.logic.data.entities.ProgramEntity

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