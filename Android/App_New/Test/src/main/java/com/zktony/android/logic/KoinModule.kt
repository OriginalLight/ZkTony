package com.zktony.android.logic

import com.zktony.android.ui.screen.HomeViewModel
import com.zktony.android.ui.screen.LcViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * @author 刘贺贺
 * @date 2023/5/19 14:51
 */

val koinModule = module {
    // task
    singleOf(::SerialPort)

    // viewModel
    viewModelOf(::HomeViewModel)
    viewModelOf(::LcViewModel)
}