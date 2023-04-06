package com.zktony.manager.di

import com.zktony.manager.R
import com.zktony.manager.common.ext.Ext
import com.zktony.manager.data.remote.*
import io.grpc.TlsChannelCredentials
import io.grpc.okhttp.OkHttpChannelBuilder
import org.koin.core.qualifier.named
import org.koin.dsl.module

val remoteModule = module {

    single {
        TlsChannelCredentials.newBuilder()
            .trustManager(Ext.ctx.resources.openRawResource(R.raw.ca))
            .build()
    }
    single(
        qualifier = named("ApplicationGrpcChannel")
    ) {
        OkHttpChannelBuilder.forAddress("182.160.14.59", 9527, get())
            .overrideAuthority("example.com")
            .build()
    }
    single(
        qualifier = named("ManagerGrpcChannel")
    ) {
        OkHttpChannelBuilder.forAddress("182.160.14.59", 9528, get())
            .overrideAuthority("example.com")
            .build()
    }
    single {
        ApplicationGrpc(get(qualifier = named("ApplicationGrpcChannel")))
    }
    single {
        CustomerGrpc(get(qualifier = named("ManagerGrpcChannel")))
    }
    single {
        SoftwareGrpc(get(qualifier = named("ManagerGrpcChannel")))
    }
    single {
        InstrumentGrpc(get(qualifier = named("ManagerGrpcChannel")))
    }
    single {
        OrderGrpc(get(qualifier = named("ManagerGrpcChannel")))
    }
}