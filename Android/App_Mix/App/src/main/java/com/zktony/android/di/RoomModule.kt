package com.zktony.android.di

import android.content.Context
import androidx.room.Room
import com.zktony.android.data.AppDatabase
import com.zktony.android.utils.Constants
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
            name = Constants.DATABASE_NAME
        ).build()
    }

    @Provides
    fun calibrationDao(database: AppDatabase) = database.CalibrationDao()

    @Provides
    fun motorDao(database: AppDatabase) = database.MotorDao()

    @Provides
    fun programDao(database: AppDatabase) = database.ProgramDao()
}