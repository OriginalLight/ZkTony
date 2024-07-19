package com.zktony.android.di

import android.content.Context
import androidx.room.Room
import com.zktony.android.data.AppDatabase
import com.zktony.android.data.MIGRATION_1_2
import com.zktony.android.data.MIGRATION_2_3
import com.zktony.android.data.MIGRATION_3_4
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
        ).addMigrations(MIGRATION_1_2).addMigrations(MIGRATION_2_3).addMigrations(MIGRATION_3_4).build()
    }

    @Provides
    fun calibrationDao(database: AppDatabase) = database.CalibrationDao()

    @Provides
    fun motorDao(database: AppDatabase) = database.MotorDao()

    @Provides
    fun programDao(database: AppDatabase) = database.ProgramDao()

    @Provides
    fun ExperimentRecordDao(database: AppDatabase) = database.ExperimentRecordDao()

    @Provides
    fun SettingDao(database: AppDatabase) = database.SettingDao()

    @Provides
    fun NewCalibrationDao(database: AppDatabase) = database.NewCalibrationDao()
    @Provides
    fun ErrorRecordDao(database: AppDatabase) = database.ErrorRecordDao()
    @Provides
    fun SportsLogDao(database: AppDatabase) = database.SportsLogDao()
    @Provides
    fun ExpectedDao(database: AppDatabase) = database.ExpectedDao()
}