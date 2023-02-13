package com.zktony.www.common.di

import androidx.lifecycle.ViewModelProvider
import com.zktony.www.common.app.AppViewModel
import com.zktony.www.common.app.AppViewModelFactory
import com.zktony.www.common.app.CommonApplicationProxy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [AppViewModel] 提供者
 */
@InstallIn(SingletonComponent::class)
@Module
object ViewModelModule {

    @Singleton
    @Provides
    fun provideAppViewModel(factory: AppViewModelFactory) =
        ViewModelProvider(CommonApplicationProxy.viewModelStore, factory)[AppViewModel::class.java]

}