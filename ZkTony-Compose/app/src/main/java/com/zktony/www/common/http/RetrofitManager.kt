package com.zktony.www.common.http

import com.zktony.www.common.Logger
import com.zktony.www.common.constant.Constants.BASE_URL
import com.zktony.www.common.http.adapter.ErrorHandler
import com.zktony.www.common.http.adapter.NetworkResponseAdapterFactory
import com.zktony.www.common.http.converter.GsonConverterFactory
import com.zktony.www.common.http.interceptor.logInterceptor
import com.zktony.www.data.services.BaseService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Retrofit管理类
 */
object RetrofitManager {

    private const val TIME_OUT_SECONDS = 10

    /** OkHttpClient相关配置 */
    private val client: OkHttpClient
        get() = OkHttpClient.Builder()
            .addInterceptor(logInterceptor)
            .connectTimeout(TIME_OUT_SECONDS.toLong(), TimeUnit.SECONDS)
            .build()

    private val servicesMap = ConcurrentHashMap<String, BaseService>()
    private val errorHandlers = mutableListOf<ErrorHandler>()

    fun init() {
        addErrorHandlerListener(ErrorToastHandler)
    }

    private fun addErrorHandlerListener(handler: ErrorHandler) {
        errorHandlers.add(handler)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseService> getService(serviceClass: Class<T>, baseUrl: String? = null): T {
        return servicesMap.getOrPut(serviceClass.name) {
            Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(NetworkResponseAdapterFactory(object : ErrorHandler {
                    override fun bizError(code: Int, msg: String) {
                        Logger.d(msg = "bizError: code:$code - msg: $msg")
                    }

                    override fun otherError(throwable: Throwable) {
                        Logger.e(msg = throwable.message.toString(), throwable = throwable)
                    }
                }))
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl ?: BASE_URL)
                .build()
                .create(serviceClass)
        } as T
    }

}