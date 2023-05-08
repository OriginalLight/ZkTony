package com.zktony.www.di

import com.zktony.www.ui.admin.AdminViewModel
import com.zktony.www.ui.home.HomeViewModel
import com.zktony.www.ui.log.LogChartViewModel
import com.zktony.www.ui.log.LogViewModel
import com.zktony.www.ui.program.DyeViewModel
import com.zktony.www.ui.program.ProgramViewModel
import com.zktony.www.ui.program.TransferViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::AdminViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::LogViewModel)
    viewModelOf(::LogChartViewModel)
    viewModelOf(::ProgramViewModel)
    viewModelOf(::TransferViewModel)
    viewModelOf(::DyeViewModel)
}