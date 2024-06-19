package com.zktony.room.di

import android.content.Context
import androidx.room.Room
import com.zktony.room.AppDatabase
import com.zktony.room.dao.FaultDao
import com.zktony.room.repository.FaultRepository
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
    fun calibrationDao(database: AppDatabase) = database.CalibrationDao()

    @Provides
    fun historyDao(database: AppDatabase) = database.HistoryDao()

    @Provides
    fun motorDao(database: AppDatabase) = database.MotorDao()

    @Provides
    fun programDao(database: AppDatabase) = database.ProgramDao()

    @Provides
    fun faultDao(database: AppDatabase) = database.FaultDao()

    // Repository
    @Provides
    fun faultRepository(faultDao: FaultDao) = FaultRepository(faultDao)
}