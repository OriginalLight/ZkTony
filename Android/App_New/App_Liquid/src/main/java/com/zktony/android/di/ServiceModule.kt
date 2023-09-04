package com.zktony.android.di

import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.utils.service.CalibrationService
import com.zktony.android.utils.service.MotorService
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
    fun motorService(dao: MotorDao) = MotorService(dao)

    @Provides
    fun serviceObserver(
        calibrationService: CalibrationService,
        motorService: MotorService
    ): ServiceObserver {
        return ServiceObserver(calibrationService, motorService)
    }
}