package com.zktony.android.di

import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.HistoryDao
import com.zktony.android.utils.service.CalibrationService
import com.zktony.android.utils.service.HistoryService
import com.zktony.android.utils.service.ServiceObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    fun calibrationService(dao: CalibrationDao) = CalibrationService(dao)

    @Provides
    fun historyService(dao: HistoryDao) = HistoryService(dao)

    @Provides
    fun serviceObserver(
        s1: CalibrationService,
        s2: HistoryService
    ): ServiceObserver {
        return ServiceObserver(s1, s2)
    }
}