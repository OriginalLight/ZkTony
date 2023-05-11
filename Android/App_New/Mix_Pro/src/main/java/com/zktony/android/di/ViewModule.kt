package com.zktony.android.di

import com.zktony.android.ui.viewmodel.CalibrationViewModel
import com.zktony.android.ui.viewmodel.ConfigViewModel
import com.zktony.android.ui.viewmodel.ContainerViewModel
import com.zktony.android.ui.viewmodel.HomeViewModel
import com.zktony.android.ui.viewmodel.MotorViewModel
import com.zktony.android.ui.viewmodel.SettingViewModel
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