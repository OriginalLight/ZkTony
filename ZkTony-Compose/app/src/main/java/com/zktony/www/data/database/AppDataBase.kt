package com.zktony.www.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.www.data.converters.DateConverters
import com.zktony.www.data.dao.*
import com.zktony.www.data.entity.*

/**
 * @author 刘贺贺
 */
@Database(
    entities = [Motor::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motorDao(): MotorDao
}