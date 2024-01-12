package com.zktony.www.di

import com.zktony.www.ui.*
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::AdminViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::LogViewModel)
    viewModelOf(::LogChartViewModel)
    viewModelOf(::ProgramViewModel)
}