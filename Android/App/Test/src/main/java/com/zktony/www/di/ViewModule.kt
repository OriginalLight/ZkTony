package com.zktony.www.di

import com.zktony.www.ui.protocol.ProtocolViewModel
import com.zktony.www.ui.tec.TecViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {
    viewModelOf(::TecViewModel)
    viewModelOf(::ProtocolViewModel)
}