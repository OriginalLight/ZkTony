package com.zktony.www.di

import com.zktony.www.manager.SerialManager
import com.zktony.www.manager.StateManager
import com.zktony.www.manager.WorkerManager
import org.koin.dsl.module

val managerModule = module {
    single { SerialManager() }
    single { WorkerManager() }
    single { StateManager(get(), get(), get()) }
}