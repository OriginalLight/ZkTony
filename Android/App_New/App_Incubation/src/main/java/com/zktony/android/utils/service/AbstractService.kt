package com.zktony.android.utils.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

abstract class AbstractService {
    val scope: CoroutineScope by lazy {
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    }
    var job: Job? = null
    open fun create() {}

    open fun destroy() {
        job?.cancel()
    }
}