package com.zktony.www.di

import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.MotorManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.Initializer
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val managerModule = module {
    singleOf(::SerialManager)
    singleOf(::MotorManager)
    singleOf(::ExecutionManager)
    singleOf(::Initializer)
}