package com.zktony.android.di

import androidx.room.Room
import com.zktony.android.data.AppDatabase
import com.zktony.android.ui.*
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
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = DATABASE_NAME
        ).build()
    }
    single { get<AppDatabase>().CurveDao() }
    single { get<AppDatabase>().HistoryDao() }
    single { get<AppDatabase>().MotorDao() }
    single { get<AppDatabase>().ProgramDao() }

    // viewModel
    viewModelOf(::CurveViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ProgramViewModel)
    viewModelOf(::SettingViewModel)
}