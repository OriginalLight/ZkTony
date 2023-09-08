package com.zktony.www.di

import com.zktony.www.core.ScheduleTask
import com.zktony.www.core.SerialPort
import com.zktony.www.core.WorkerManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    singleOf(::SerialPort)
    singleOf(::WorkerManager)
    singleOf(::ScheduleTask)
}