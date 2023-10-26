package com.zktony.www.di

import com.zktony.www.ui.admin.*
import com.zktony.www.ui.calibration.CalibrationDataViewModel
import com.zktony.www.ui.calibration.CalibrationViewModel
import com.zktony.www.ui.container.ContainerEditViewModel
import com.zktony.www.ui.container.ContainerViewModel
import com.zktony.www.ui.home.HomeViewModel
import com.zktony.www.ui.program.*
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::AdminViewModel)
    viewModelOf(::CalibrationDataViewModel)
    viewModelOf(::CalibrationViewModel)
    viewModelOf(::ConfigViewModel)
    viewModelOf(::ContainerEditViewModel)
    viewModelOf(::ContainerViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MotorViewModel)
    viewModelOf(::ProgramEditViewModel)
    viewModelOf(::ProgramPointViewModel)
    viewModelOf(::ProgramViewModel)
}