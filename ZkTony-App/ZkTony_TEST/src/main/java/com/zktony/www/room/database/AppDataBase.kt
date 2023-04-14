package com.zktony.www.room.database

import androidx.room.*
import com.zktony.room.converters.DateConverters
import com.zktony.www.room.dao.TestDao
import com.zktony.www.room.entity.Test

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