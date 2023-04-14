package com.zktony.www.manager

class Initializer constructor(
    private val SM: SerialManager,
    private val WM: WorkerManager,
) {
    fun init() {
        SM.initializer()
        WM.initializer()
    }
}