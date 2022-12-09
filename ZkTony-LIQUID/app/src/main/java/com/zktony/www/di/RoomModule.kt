/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zktony.www.di

import android.content.Context
import androidx.room.Room
import com.zktony.www.common.room.dao.*
import com.zktony.www.common.room.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "zktony.db"
        ).build()
    }

    @Provides
    fun provideLogRecordDao(database: AppDatabase): LogDao {
        return database.logDao()
    }

    @Provides
    fun provideProgramDao(database: AppDatabase): ProgramDao {
        return database.programDao()
    }

    @Provides
    fun provideActionDao(database: AppDatabase): ActionDao {
        return database.actionDao()
    }

    @Provides
    fun provideMotorDao(database: AppDatabase): MotorDao {
        return database.motorDao()
    }

    @Provides
    fun provideCalibrationDao(database: AppDatabase): CalibrationDao {
        return database.calibrationDao()
    }
}