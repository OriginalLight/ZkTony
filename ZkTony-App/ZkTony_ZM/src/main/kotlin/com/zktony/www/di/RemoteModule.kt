package com.zktony.www.di

import com.google.gson.GsonBuilder
import com.zktony.common.R
import com.zktony.common.ext.Ext
import com.zktony.common.http.adapter.FlowCallAdapterFactory
import com.zktony.common.utils.Constants
import com.zktony.www.BuildConfig
import com.zktony.www.data.remote.grpc.ApplicationGrpc
import com.zktony.www.data.remote.service.ApplicationService
import com.zktony.www.data.remote.service.LogService
import com.zktony.www.data.remote.service.ProgramService
import io.grpc.TlsChannelCredentials
import io.grpc.okhttp.OkHttpChannelBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.net.ssl.KeyManagerFactory

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
    single {
        TlsChannelCredentials.newBuilder()
            .trustManager(Ext.ctx.resources.openRawResource(R.raw.ca))
            .build()
    }
    single {
        OkHttpChannelBuilder.forAddress("182.160.14.59", 9527, get())
            .overrideAuthority("example.com")
            .build()
    }
    single {
        ApplicationGrpc(get())
    }
    single { get<Retrofit>().create(ApplicationService::class.java) }
    single { get<Retrofit>().create(ProgramService::class.java) }
    single { get<Retrofit>().create(LogService::class.java) }
}