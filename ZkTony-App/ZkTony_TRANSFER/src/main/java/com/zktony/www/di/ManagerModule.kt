package com.zktony.www.di

import com.zktony.www.manager.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val managerModule = module {
    singleOf(::SerialManager)
    singleOf(::WorkerManager)
    singleOf(::Initializer)
}