package com.zktony.www.di

import com.zktony.www.ui.AdminViewModel
import com.zktony.www.ui.HomeViewModel
import com.zktony.www.ui.LogChartViewModel
import com.zktony.www.ui.LogViewModel
import com.zktony.www.ui.DyeViewModel
import com.zktony.www.ui.ProgramViewModel
import com.zktony.www.ui.TransferViewModel
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