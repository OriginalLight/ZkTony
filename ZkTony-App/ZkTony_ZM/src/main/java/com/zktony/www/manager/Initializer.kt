package com.zktony.www.manager

class Initializer constructor(
    private val serialManager: SerialManager,
    private val workerManager: WorkerManager,
) {
    fun init() {
        serialManager.init()
        workerManager.init()
    }
}