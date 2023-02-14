package com.zktony.manager.data.remote.client

import com.zktony.manager.BuildConfig
import okhttp3.OkHttpClient
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.annotation.DefaultDomain
import java.util.concurrent.TimeUnit


/**
 * @author: 刘贺贺
 * @date: 2023-02-14 13:11
 */
class RxHttpClient {

    fun init() {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        RxHttpPlugins.init(okHttpClient)
            .setDebug(BuildConfig.DEBUG, false, 2)

    }



    companion object {
        @DefaultDomain
        const val BASE_URL = "http://182.160.14.59:30765"

        @JvmStatic
        val instance: RxHttpClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RxHttpClient()
        }
    }

}
