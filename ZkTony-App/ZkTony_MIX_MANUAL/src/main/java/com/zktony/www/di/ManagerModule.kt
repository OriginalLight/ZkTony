package com.zktony.www.di

import com.zktony.www.manager.ExecutionManager
import com.zktony.www.manager.MotorManager
import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.StateManager
import org.koin.dsl.module

val managerModule = module {
    single { SerialManager() }
    single { MotorManager(get(), get()) }
    single { ExecutionManager(get(), get()) }
    single { StateManager(get(), get(), get(), get()) }
}