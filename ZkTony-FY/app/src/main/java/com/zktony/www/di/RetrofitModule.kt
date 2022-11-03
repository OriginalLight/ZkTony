package com.zktony.www.di

import com.zktony.www.BuildConfig
import com.zktony.www.common.network.adapter.ErrorHandler
import com.zktony.www.common.network.adapter.NetworkResponseAdapterFactory
import com.zktony.www.common.network.converter.GsonConverterFactory
import com.zktony.www.common.network.interceptor.logInterceptor
import com.zktony.www.common.network.service.LogService
import com.zktony.www.common.network.service.ProgramService
import com.zktony.www.common.network.service.SystemService
import com.zktony.www.common.utils.Constants
import com.zktony.www.common.utils.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RetrofitModule {

    @Singleton
    @Provides
    fun getOkHttpClient() = if (BuildConfig.DEBUG) {
        OkHttpClient.Builder()
            .addInterceptor(logInterceptor)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    } else {
        OkHttpClient.Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun getRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideSystemService(retrofit: Retrofit): SystemService {
        return retrofit.create(SystemService::class.java)
    }

    @Singleton
    @Provides
    fun provideLogService(retrofit: Retrofit): LogService {
        return retrofit.create(LogService::class.java)
    }

    @Singleton
    @Provides
    fun provideProgramService(retrofit: Retrofit): ProgramService {
        return retrofit.create(ProgramService::class.java)
    }

}