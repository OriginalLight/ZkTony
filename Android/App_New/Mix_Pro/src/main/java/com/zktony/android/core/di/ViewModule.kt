package com.zktony.android.core.di

import com.zktony.android.ui.viewmodel.AdminViewModel
import com.zktony.android.ui.viewmodel.ConfigViewModel
import com.zktony.android.ui.viewmodel.HomeViewModel
import com.zktony.android.ui.viewmodel.MotorViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::AdminViewModel)
    viewModelOf(::MotorViewModel)
    viewModelOf(::ConfigViewModel)
}