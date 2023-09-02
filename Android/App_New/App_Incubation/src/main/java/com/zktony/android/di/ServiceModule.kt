package com.zktony.android.di

import com.zktony.android.data.dao.CurveDao
import com.zktony.android.data.dao.HistoryDao
import com.zktony.android.utils.service.CurveService
import com.zktony.android.utils.service.HistoryService
import com.zktony.android.utils.service.ServiceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    fun curveService(dao: CurveDao) = CurveService(dao)

    @Provides
    fun historyService(dao: HistoryDao) = HistoryService(dao)

    @Provides
    fun service(
        curveService: CurveService,
        historyService: HistoryService
    ) = ServiceFactory(curveService, historyService)
}