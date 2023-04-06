package com.zktony.manager.di

import com.zktony.manager.ui.screen.viewmodel.HomeViewModel
import com.zktony.manager.ui.screen.viewmodel.SettingViewModel
import com.zktony.manager.ui.screen.viewmodel.ShippingHistoryViewModel
import com.zktony.manager.ui.screen.viewmodel.ShippingViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingViewModel)
    viewModelOf(::ShippingViewModel)
    viewModelOf(::ShippingHistoryViewModel)
}