package com.zktony.www.manager

class Initializer constructor(
    private val SM: SerialManager,
    private val MM: MotorManager,
) {
    fun init() {
        SM.initializer()
        MM.initializer()
    }
}