package com.zktony.www.di

import com.zktony.www.common.Logger
import com.zktony.www.common.constant.Constants
import com.zktony.www.common.http.adapter.ErrorHandler
import com.zktony.www.common.http.adapter.NetworkResponseAdapterFactory
import com.zktony.www.common.http.converter.GsonConverterFactory
import com.zktony.www.common.http.interceptor.logInterceptor
import com.zktony.www.data.services.LogService
import com.zktony.www.data.services.ProgramService
import com.zktony.www.data.services.SystemService
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
            .addCallAdapterFactory(NetworkResponseAdapterFactory(object : ErrorHandler {
                override fun bizError(code: Int, msg: String) {
                    Logger.d(msg = "bizError: code:$code - msg: $msg")
                }

                override fun otherError(throwable: Throwable) {
                    Logger.e(msg = throwable.message.toString(), throwable = throwable)
                }
            }))
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