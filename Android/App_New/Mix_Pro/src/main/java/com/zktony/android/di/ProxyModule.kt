package com.zktony.android.di

import com.zktony.android.proxy.MCProxy
import com.zktony.android.proxy.SerialProxy
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val proxyModule = module {
    singleOf(::SerialProxy)
    singleOf(::MCProxy)
}