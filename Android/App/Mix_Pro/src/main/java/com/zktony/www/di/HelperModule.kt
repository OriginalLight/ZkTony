package com.zktony.www.di

import com.zktony.www.proxy.MCProxy
import com.zktony.www.proxy.SerialProxy
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val helperModule = module {
    singleOf(::SerialProxy)
    singleOf(::MCProxy)
}