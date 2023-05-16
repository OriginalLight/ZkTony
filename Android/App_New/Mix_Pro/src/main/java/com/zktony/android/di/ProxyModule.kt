package com.zktony.android.di

import com.zktony.android.core.ScheduleTask
import com.zktony.android.core.SerialPort
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val proxyModule = module {
    singleOf(::SerialPort)
    singleOf(::ScheduleTask)
}