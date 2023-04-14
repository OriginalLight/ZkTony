package com.zktony.www.di

import com.zktony.www.ui.admin.*
import com.zktony.www.ui.calibration.CalibrationDataViewModel
import com.zktony.www.ui.calibration.CalibrationViewModel
import com.zktony.www.ui.home.HomeViewModel
import com.zktony.www.ui.log.LogViewModel
import com.zktony.www.ui.program.ActionViewModel
import com.zktony.www.ui.program.ProgramViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::AdminViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MotorViewModel)
    viewModelOf(::CalibrationViewModel)
    viewModelOf(::CalibrationDataViewModel)
    viewModelOf(::ContainerViewModel)
    viewModelOf(::LogViewModel)
    viewModelOf(::ProgramViewModel)
    viewModelOf(::ActionViewModel)
}