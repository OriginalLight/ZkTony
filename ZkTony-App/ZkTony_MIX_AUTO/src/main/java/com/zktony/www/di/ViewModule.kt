package com.zktony.www.di

import com.zktony.www.ui.admin.AdminViewModel
import com.zktony.www.ui.admin.MotorViewModel
import com.zktony.www.ui.calibration.CalibrationDataViewModel
import com.zktony.www.ui.calibration.CalibrationViewModel
import com.zktony.www.ui.container.*
import com.zktony.www.ui.home.HomeViewModel
import com.zktony.www.ui.log.LogViewModel
import com.zktony.www.ui.program.ProgramEditViewModel
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
    viewModelOf(::ContainerEditViewModel)
    viewModelOf(::WashViewModel)
    viewModelOf(::LogViewModel)
    viewModelOf(::ProgramViewModel)
    viewModelOf(::ProgramEditViewModel)
}