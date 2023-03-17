package com.zktony.www.di

import com.google.gson.GsonBuilder
import com.zktony.common.http.adapter.FlowCallAdapterFactory
import com.zktony.common.utils.Constants
import com.zktony.www.BuildConfig
import com.zktony.www.data.remote.service.ApplicationService
import com.zktony.www.data.remote.service.LogService
import com.zktony.www.data.remote.service.ProgramService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RetrofitModule {

    @Singleton
    @Provides
    fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    @Singleton
    @Provides
    fun getRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(FlowCallAdapterFactory.createAsync())
            .addConverterFactory(GsonConverterFactory.create(
                GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create()
            ))
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