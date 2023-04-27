package com.zktony.www.di

import com.zktony.www.ui.admin.*
import com.zktony.www.ui.calibration.CalibrationDataViewModel
import com.zktony.www.ui.calibration.CalibrationViewModel
import com.zktony.www.ui.container.ContainerEditViewModel
import com.zktony.www.ui.container.ContainerViewModel
import com.zktony.www.ui.home.HomeViewModel
import com.zktony.www.ui.program.ProgramEditViewModel
import com.zktony.www.ui.program.ProgramViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::AdminViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MotorViewModel)
    viewModelOf(::ConfigViewModel)
    viewModelOf(::CalibrationViewModel)
    viewModelOf(::CalibrationDataViewModel)
    viewModelOf(::ContainerViewModel)
    viewModelOf(::ContainerEditViewModel)
    viewModelOf(::ProgramViewModel)
    viewModelOf(::ProgramEditViewModel)
}