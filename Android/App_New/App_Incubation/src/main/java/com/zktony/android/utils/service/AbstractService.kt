package com.zktony.android.utils.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class AbstractService {
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    open fun start() {}
}