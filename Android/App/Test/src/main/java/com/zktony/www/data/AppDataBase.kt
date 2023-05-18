package com.zktony.www.data

import androidx.room.*
import com.zktony.www.data.DateConverters
import com.zktony.www.data.dao.TestDao
import com.zktony.www.data.entities.Test

/**
 * @author 刘贺贺
 */
@Database(
    entities = [Test::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(com.zktony.www.data.DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testDao(): TestDao
}