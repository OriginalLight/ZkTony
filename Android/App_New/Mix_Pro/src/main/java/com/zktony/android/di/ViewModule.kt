package com.zktony.android.di

import com.zktony.android.ui.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::AdminViewModel)
}