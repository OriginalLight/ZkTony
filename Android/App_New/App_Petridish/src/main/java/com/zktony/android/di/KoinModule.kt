package com.zktony.android.di

import androidx.room.Room
import com.zktony.android.data.AppDatabase
import com.zktony.android.ui.CalibrationViewModel
import com.zktony.android.ui.HomeViewModel
import com.zktony.android.ui.ProgramViewModel
import com.zktony.android.ui.SettingViewModel
import com.zktony.android.ui.SplashViewModel
import com.zktony.android.utils.Constants.DATABASE_NAME
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
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
    single { get<AppDatabase>().CalibrationDao() }
    single { get<AppDatabase>().MotorDao() }
    single { get<AppDatabase>().ProgramDao() }

    // viewModel
    viewModelOf(::CalibrationViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ProgramViewModel)
    viewModelOf(::SettingViewModel)
    viewModelOf(::SplashViewModel)
}

