package com.zktony.www.core.ext

import com.zktony.www.core.SerialPort
import com.zktony.www.core.WorkerManager
import org.koin.java.KoinJavaComponent.inject

val serialPort: SerialPort by inject(SerialPort::class.java)
val workerManager: WorkerManager by inject(WorkerManager::class.java)
