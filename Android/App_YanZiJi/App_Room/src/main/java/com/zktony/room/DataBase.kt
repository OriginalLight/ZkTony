package com.zktony.room

import androidx.room.*
import com.zktony.room.dao.*
import com.zktony.room.entities.*

/**
 * @author 刘贺贺
 */
@Database(
    entities =
    [History::class, Program::class, Fault::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun HistoryDao(): HistoryDao
    abstract fun ProgramDao(): ProgramDao
    abstract fun FaultDao(): FaultDao
    abstract fun UserDao(): UserDao
}