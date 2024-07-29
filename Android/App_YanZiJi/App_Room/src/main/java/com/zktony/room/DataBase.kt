package com.zktony.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zktony.room.dao.FaultDao
import com.zktony.room.dao.HistoryDao
import com.zktony.room.dao.ProgramDao
import com.zktony.room.dao.UserDao
import com.zktony.room.entities.Fault
import com.zktony.room.entities.History
import com.zktony.room.entities.Program
import com.zktony.room.entities.User

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