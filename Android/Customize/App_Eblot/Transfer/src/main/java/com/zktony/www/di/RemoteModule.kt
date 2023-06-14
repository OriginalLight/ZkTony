package com.zktony.www.di

import com.zktony.core.utils.Constants.GRPC_AUTHORITY
import com.zktony.core.utils.Constants.GRPC_HOST
import com.zktony.core.utils.Constants.GRPC_PORT
import com.zktony.protobuf.grpc.ApplicationGrpc
import com.zktony.protobuf.grpc.LogDetailGrpc
import com.zktony.protobuf.grpc.LogGrpc
import com.zktony.protobuf.grpc.ProgramGrpc
import com.zktony.www.R
import io.grpc.TlsChannelCredentials
import io.grpc.okhttp.OkHttpChannelBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val remoteModule = module {
    single {
        TlsChannelCredentials.newBuilder()
            .trustManager(androidContext().resources.openRawResource(R.raw.ca))
            .build()
    }
    single {
        OkHttpChannelBuilder.forAddress(GRPC_HOST, GRPC_PORT, get())
            .overrideAuthority(GRPC_AUTHORITY)
            .build()
    }
    single {
        ApplicationGrpc(get())
    }
    single {
        LogGrpc(get())
    }
    single {
        LogDetailGrpc(get())
    }
    single {
        ProgramGrpc(get())
    }
}