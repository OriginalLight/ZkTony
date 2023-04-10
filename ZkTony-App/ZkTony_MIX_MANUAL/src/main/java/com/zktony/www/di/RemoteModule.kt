package com.zktony.www.di

import com.zktony.core.R
import com.zktony.core.ext.Ext
import com.zktony.core.utils.Constants.GRPC_AUTHORITY
import com.zktony.core.utils.Constants.GRPC_HOST
import com.zktony.core.utils.Constants.GRPC_PORT
import com.zktony.protobuf.grpc.ApplicationGrpc
import io.grpc.TlsChannelCredentials
import io.grpc.okhttp.OkHttpChannelBuilder
import org.koin.dsl.module

val remoteModule = module {
    single {
        TlsChannelCredentials.newBuilder()
            .trustManager(Ext.ctx.resources.openRawResource(R.raw.ca))
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
}