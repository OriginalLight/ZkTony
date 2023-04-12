package com.zktony.www.manager

class Initializer constructor(
    private val serialManager: SerialManager,
    private val motorManager: MotorManager,
    private val executionManager: ExecutionManager,
    private val containerManager: ContainerManager,
    private val workerManager: WorkerManager,
) {
    fun init() {
        serialManager.init()
        motorManager.init()
        executionManager.init()
        containerManager.init()
        workerManager.init()
    }
}