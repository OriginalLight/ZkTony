package com.zktony.www.manager

class Initializer constructor(
    private val SM: SerialManager,
    private val MM: MotorManager,
    private val EM: ExecutionManager,
    private val CM: ContainerManager,
    private val WM: WorkerManager,
) {
    fun init() {
        SM.initializer()
        MM.initializer()
        EM.initializer()
        CM.initializer()
        WM.initializer()
    }
}