package com.zktony.manager.di

import com.google.gson.GsonBuilder
import com.zktony.manager.BuildConfig
import com.zktony.manager.data.remote.adapter.FlowCallAdapterFactory
import com.zktony.manager.data.remote.service.ApplicationService
import com.zktony.manager.data.remote.service.SoftwareService
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

    private const val BASE_URL = "http://182.160.14.59:30765"


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
            .baseUrl(BASE_URL)
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
    fun provideSoftwareService(retrofit: Retrofit): SoftwareService {
        return retrofit.create(SoftwareService::class.java)
    }

}