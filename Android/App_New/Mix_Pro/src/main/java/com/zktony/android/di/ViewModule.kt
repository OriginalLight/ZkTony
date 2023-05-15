package com.zktony.android.di

import com.zktony.android.ui.screen.CalibrationViewModel
import com.zktony.android.ui.screen.ConfigViewModel
import com.zktony.android.ui.screen.ContainerViewModel
import com.zktony.android.ui.screen.HomeViewModel
import com.zktony.android.ui.screen.MotorViewModel
import com.zktony.android.ui.screen.ProgramViewModel
import com.zktony.android.ui.screen.SettingViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingViewModel)
    viewModelOf(::MotorViewModel)
    viewModelOf(::ConfigViewModel)
    viewModelOf(::CalibrationViewModel)
    viewModelOf(::ContainerViewModel)
    viewModelOf(::ProgramViewModel)
}