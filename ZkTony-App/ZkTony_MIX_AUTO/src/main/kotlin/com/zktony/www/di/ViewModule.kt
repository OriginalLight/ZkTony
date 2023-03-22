package com.zktony.www.di

import com.zktony.www.ui.admin.AdminViewModel
import com.zktony.www.ui.admin.MotorViewModel
import com.zktony.www.ui.app.AppViewModel
import com.zktony.www.ui.calibration.CalibrationDataViewModel
import com.zktony.www.ui.calibration.CalibrationViewModel
import com.zktony.www.ui.container.ContainerViewModel
import com.zktony.www.ui.container.PlateViewModel
import com.zktony.www.ui.container.WashViewModel
import com.zktony.www.ui.home.HomeViewModel
import com.zktony.www.ui.log.LogViewModel
import com.zktony.www.ui.program.ProgramPlateViewModel
import com.zktony.www.ui.program.ProgramViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::AppViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::AdminViewModel)
    viewModelOf(::MotorViewModel)
    viewModelOf(::CalibrationViewModel)
    viewModelOf(::CalibrationDataViewModel)
    viewModelOf(::ContainerViewModel)
    viewModelOf(::PlateViewModel)
    viewModelOf(::WashViewModel)
    viewModelOf(::LogViewModel)
    viewModelOf(::ProgramViewModel)
    viewModelOf(::ProgramPlateViewModel)
}