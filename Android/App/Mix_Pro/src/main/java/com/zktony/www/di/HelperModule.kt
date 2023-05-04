package com.zktony.www.di

import com.zktony.www.helper.MCHelper
import com.zktony.www.helper.SerialHelper
import org.koin.core.module.dsl.DefinitionOptions
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val helperModule = module {
    singleOf(::SerialHelper)
    singleOf(::MCHelper)
}