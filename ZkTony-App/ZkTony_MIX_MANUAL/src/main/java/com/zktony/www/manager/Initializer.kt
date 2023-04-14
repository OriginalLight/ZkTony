package com.zktony.www.manager

class Initializer constructor(
    private val executionManager: ExecutionManager,
    private val motorManager: MotorManager,
    private val serialManager: SerialManager,
) {
    fun init() {
        serialManager.init()
        motorManager.init()
        executionManager.init()
    }
}