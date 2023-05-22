package com.zktony.android.logic

import androidx.room.Room
import com.zktony.android.R
import com.zktony.android.logic.data.AppDatabase
import com.zktony.android.ui.screen.CalibrationViewModel
import com.zktony.android.ui.screen.ConfigViewModel
import com.zktony.android.ui.screen.ContainerViewModel
import com.zktony.android.ui.screen.HomeViewModel
import com.zktony.android.ui.screen.MotorViewModel
import com.zktony.android.ui.screen.ProgramViewModel
import com.zktony.android.ui.screen.SettingViewModel
import com.zktony.core.utils.Constants
import com.zktony.protobuf.grpc.ApplicationGrpc
import io.grpc.TlsChannelCredentials
import io.grpc.okhttp.OkHttpChannelBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * @author 刘贺贺
 * @date 2023/5/19 14:51
 */

val koinModule = module {
    // data
    single {
        Room.databaseBuilder(
            androidContext(), AppDatabase::class.java, "data.db"
        ).build()
    }
    single { get<AppDatabase>().motorDao() }
    single { get<AppDatabase>().calibrationDao() }
    single { get<AppDatabase>().containerDao() }
    single { get<AppDatabase>().programDao() }

    // remote
    single {
        OkHttpChannelBuilder
            .forAddress(
                Constants.GRPC_HOST,
                Constants.GRPC_PORT,
                TlsChannelCredentials.newBuilder()
                    .trustManager(androidContext().resources.openRawResource(R.raw.ca))
                    .build(),
            )
            .overrideAuthority(Constants.GRPC_AUTHORITY)
            .build()
    }
    singleOf(::ApplicationGrpc)

    // task
    singleOf(::SerialPort)
    singleOf(::ScheduleTask)

    // viewModel
    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingViewModel)
    viewModelOf(::MotorViewModel)
    viewModelOf(::ConfigViewModel)
    viewModelOf(::CalibrationViewModel)
    viewModelOf(::ContainerViewModel)
    viewModelOf(::ProgramViewModel)
}