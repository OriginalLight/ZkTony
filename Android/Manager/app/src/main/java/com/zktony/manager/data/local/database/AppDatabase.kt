package com.zktony.manager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.manager.data.local.dao.UserDao
import com.zktony.manager.data.local.entity.User

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 10:08
 */
/**
 * The [RoomDatabase] we use in this app.
 */
@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}