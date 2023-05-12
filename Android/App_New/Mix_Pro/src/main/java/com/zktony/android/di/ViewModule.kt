package com.zktony.android.di

import com.zktony.android.ui.screen.calibration.CalibrationViewModel
import com.zktony.android.ui.screen.config.ConfigViewModel
import com.zktony.android.ui.screen.container.ContainerViewModel
import com.zktony.android.ui.screen.home.HomeViewModel
import com.zktony.android.ui.screen.motor.MotorViewModel
import com.zktony.android.ui.screen.setting.SettingViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingViewModel)
    viewModelOf(::MotorViewModel)
    viewModelOf(::ConfigViewModel)
    viewModelOf(::CalibrationViewModel)
    viewModelOf(::ContainerViewModel)
}