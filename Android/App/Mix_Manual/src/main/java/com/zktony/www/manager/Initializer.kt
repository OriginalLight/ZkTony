package com.zktony.www.manager

class Initializer constructor(
    private val EM: ExecutionManager,
    private val MM: MotorManager,
    private val SM: SerialManager,
) {
    fun init() {
        SM.initializer()
        MM.initializer()
        EM.initializer()
    }
}