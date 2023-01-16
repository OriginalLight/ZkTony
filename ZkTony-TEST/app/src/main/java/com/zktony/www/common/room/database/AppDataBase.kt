package com.zktony.www.common.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.www.common.room.converters.DateConverters
import com.zktony.www.common.room.dao.*
import com.zktony.www.common.room.entity.*

/**
 * @author 刘贺贺
 */
@Database(
    entities = [Test::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testDao(): TestDao
}