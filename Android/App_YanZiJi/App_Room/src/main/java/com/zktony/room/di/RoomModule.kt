package com.zktony.room.di

import android.content.Context
import androidx.room.Room
import com.zktony.room.AppDatabase
import com.zktony.room.dao.ErrorLogDao
import com.zktony.room.dao.LogDao
import com.zktony.room.dao.LogSnapshotDao
import com.zktony.room.dao.UserDao
import com.zktony.room.repository.ErrorLogRepository
import com.zktony.room.repository.LogRepository
import com.zktony.room.repository.LogSnapshotRepository
import com.zktony.room.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            context = appContext,
            klass = AppDatabase::class.java,
            name = "njzkty-db"
        ).build()
    }

    @Provides
    fun programDao(database: AppDatabase) = database.ProgramDao()

    @Provides
    fun errorLogDao(database: AppDatabase) = database.errorLogDao()

    @Provides
    fun logDao(database: AppDatabase) = database.logDao()

    @Provides
    fun logSnapshotDao(database: AppDatabase) = database.logSnapshotDao()

    @Provides
    fun userDao(database: AppDatabase) = database.UserDao()

    // Repository
    @Provides
    fun errorLogRepository(errorLogDao: ErrorLogDao) = ErrorLogRepository(errorLogDao)

    @Provides
    fun logRepository(logDao: LogDao) = LogRepository(logDao)

    @Provides
    fun logSnapshotRepository(logSnapshotDao: LogSnapshotDao) = LogSnapshotRepository(logSnapshotDao)

    @Provides
    fun userRepository(userDao: UserDao) = UserRepository(userDao)
}