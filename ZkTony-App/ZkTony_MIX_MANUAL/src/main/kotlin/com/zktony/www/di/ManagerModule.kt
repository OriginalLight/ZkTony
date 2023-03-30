package com.zktony.www.di

import com.zktony.www.manager.*
import org.koin.dsl.module

val managerModule = module {
    single { SerialManager() }
    single { MotorManager(get(), get()) }
    single { ExecutionManager(get(), get()) }
    single { StateManager(get(), get(), get(), get()) }
}