package com.zktony.www.di

import com.zktony.www.proxy.MCProxy
import com.zktony.www.proxy.SerialProxy
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val proxyModule = module {
    singleOf(::SerialProxy)
    singleOf(::MCProxy)
}