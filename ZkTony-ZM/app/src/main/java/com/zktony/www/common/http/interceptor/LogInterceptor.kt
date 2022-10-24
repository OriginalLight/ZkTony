package com.zktony.www.common.http.interceptor

import com.zktony.www.BuildConfig
import com.zktony.www.common.Logger
import okhttp3.logging.HttpLoggingInterceptor

val logInterceptor: HttpLoggingInterceptor by lazy {
    HttpLoggingInterceptor { Logger.d(msg = it) }.setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC)
}