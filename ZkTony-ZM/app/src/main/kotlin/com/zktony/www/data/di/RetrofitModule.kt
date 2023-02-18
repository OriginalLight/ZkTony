package com.zktony.www.data.di

import com.zktony.www.common.utils.Constants
import com.zktony.www.data.remote.adapter.NetworkResponseAdapterFactory
import com.zktony.www.data.remote.converter.GsonConverterFactory
import com.zktony.www.data.remote.interceptor.logInterceptor
import com.zktony.www.data.remote.service.ApplicationService
import com.zktony.www.data.remote.service.LogService
import com.zktony.www.data.remote.service.ProgramService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RetrofitModule {

    @Singleton
    @Provides
    fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logInterceptor)
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
    fun provideApplicationService(retrofit: Retrofit): ApplicationService {
        return retrofit.create(ApplicationService::class.java)
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