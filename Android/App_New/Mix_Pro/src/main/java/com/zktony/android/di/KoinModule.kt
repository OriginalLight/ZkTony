package com.zktony.android.di

import androidx.room.Room
import com.zktony.android.data.AppDatabase
import com.zktony.android.ext.ScheduleTask
import com.zktony.android.ext.SerialPort
import com.zktony.android.ext.utils.Constants.DATABASE_NAME
import com.zktony.android.ui.CalibrationViewModel
import com.zktony.android.ui.ConfigViewModel
import com.zktony.android.ui.HomeViewModel
import com.zktony.android.ui.MotorViewModel
import com.zktony.android.ui.ProgramViewModel
import com.zktony.android.ui.SettingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * @author 刘贺贺
 * @date 2023/5/19 14:51
 */

val koinModule = module {
    // data
    single {
        Room.databaseBuilder(
            androidContext(), AppDatabase::class.java, DATABASE_NAME
        ).build()
    }
    single { get<AppDatabase>().calibrationDao() }
    single { get<AppDatabase>().motorDao() }
    single { get<AppDatabase>().programDao() }

    // task
    singleOf(::ScheduleTask)
    singleOf(::SerialPort)

    // viewModel
    viewModelOf(::CalibrationViewModel)
    viewModelOf(::ConfigViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MotorViewModel)
    viewModelOf(::ProgramViewModel)
    viewModelOf(::SettingViewModel)
}