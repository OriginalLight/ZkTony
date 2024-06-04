package com.zktony.serialport

import com.zktony.serialport.config.SerialConfig

/**
 * Serial port helper class (abstract class) DSL
 * @param config SerialConfig
 */
inline fun serialPortOf(config: SerialConfig.() -> Unit): SerialPortImpl? {
    val serialConfig = SerialConfig().apply(config)
    val impl = SerialPortImpl()
    return if (impl.openDevice(serialConfig) == 0) {
        impl
    } else {
        impl.close()
        null
    }
}