package com.zktony.www.common.ext

import com.zktony.www.proxy.SerialProxy
import com.zktony.www.proxy.WorkerProxy
import org.koin.java.KoinJavaComponent.inject

private val sp: SerialProxy by inject(SerialProxy::class.java)
private val wp: WorkerProxy by inject(WorkerProxy::class.java)

/**
 * 初始化q
 */
fun proxyInitializer() {
    sp.initializer()
    wp.initializer()
}
