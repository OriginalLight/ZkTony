package com.zktony.www.common.http.interceptor

import com.zktony.www.BuildConfig
import com.zktony.www.common.AppLog
import okhttp3.logging.HttpLoggingInterceptor

val logInterceptor: HttpLoggingInterceptor by lazy {
    HttpLoggingInterceptor { AppLog.d(msg = it) }.setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC)
}