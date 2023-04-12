package com.zktony.www.manager

class Initializer constructor(
    private val serialManager: SerialManager,
    private val motorManager: MotorManager,
    private val executionManager: ExecutionManager,
) {
    fun init() {
        serialManager.init()
        motorManager.init()
        executionManager.init()
    }
}