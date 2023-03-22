package com.zktony.www.di

import com.google.gson.GsonBuilder
import com.zktony.common.http.adapter.FlowCallAdapterFactory
import com.zktony.common.utils.Constants
import com.zktony.www.BuildConfig
import com.zktony.www.data.remote.service.ApplicationService
import com.zktony.www.data.remote.service.LogService
import com.zktony.www.data.remote.service.ProgramService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val remoteModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level =
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(get())
            .addCallAdapterFactory(FlowCallAdapterFactory.createAsync())
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create()
                )
            )
            .build()
    }
    single { get<Retrofit>().create(ApplicationService::class.java) }
    single { get<Retrofit>().create(ProgramService::class.java) }
    single { get<Retrofit>().create(LogService::class.java) }
}