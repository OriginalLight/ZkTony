package com.zktony.android.di

import com.zktony.android.R
import com.zktony.core.utils.Constants
import com.zktony.protobuf.grpc.ApplicationGrpc
import io.grpc.TlsChannelCredentials
import io.grpc.okhttp.OkHttpChannelBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val remoteModule = module {

    single {
        TlsChannelCredentials.newBuilder()
            .trustManager(androidContext().resources.openRawResource(R.raw.ca))
            .build()
    }
    single {
        OkHttpChannelBuilder.forAddress(Constants.GRPC_HOST, Constants.GRPC_PORT, get())
            .overrideAuthority(Constants.GRPC_AUTHORITY)
            .build()
    }
    singleOf(::ApplicationGrpc)
}