package com.zktony.manager.di

import com.zktony.manager.ui.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::ManagerViewModel)
    viewModelOf(::SettingViewModel)
    viewModelOf(::OrderViewModel)
    viewModelOf(::OrderHistoryViewModel)
    viewModelOf(::UpgradeViewModel)
    viewModelOf(::UserViewModel)
    viewModelOf(::CustomerViewModel)
    viewModelOf(::InstrumentViewModel)
    viewModelOf(::SoftwareViewModel)
}