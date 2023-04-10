package com.zktony.www.di

import com.zktony.www.common.worker.LogDataWorker
import com.zktony.www.common.worker.LogRecordWorker
import com.zktony.www.common.worker.LogWorker
import com.zktony.www.common.worker.ProgramWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {
    worker { LogWorker(get(), get(), androidContext(), get()) }
    worker { LogDataWorker(get(), get(), androidContext(), get()) }
    worker { LogRecordWorker(get(), get(), androidContext(), get()) }
    worker { ProgramWorker(get(), get(), androidContext(), get()) }
}