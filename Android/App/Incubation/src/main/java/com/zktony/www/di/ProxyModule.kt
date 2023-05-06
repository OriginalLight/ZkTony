package com.zktony.www.di

import com.zktony.www.proxy.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val proxyModule = module {
    singleOf(::SerialProxy)
    singleOf(::WorkerProxy)
    singleOf(::MCProxy)
    singleOf(::ContainerProxy)
}