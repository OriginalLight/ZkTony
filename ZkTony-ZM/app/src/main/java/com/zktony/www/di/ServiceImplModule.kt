package com.zktony.www.di

import com.zktony.www.services.LogService
import com.zktony.www.services.ProgramService
import com.zktony.www.services.SystemService
import com.zktony.www.services.impl.LogServiceImpl
import com.zktony.www.services.impl.ProgramServiceImpl
import com.zktony.www.services.impl.SystemServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Service 实现提供者
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class ServiceImplModule {

    @Binds
    @Singleton
    abstract fun getSystemServiceImpl(impl: SystemServiceImpl): SystemService

    @Binds
    @Singleton
    abstract fun getLogServiceImpl(impl: LogServiceImpl): LogService

    @Binds
    @Singleton
    abstract fun getProgramServiceImpl(impl: ProgramServiceImpl): ProgramService

}