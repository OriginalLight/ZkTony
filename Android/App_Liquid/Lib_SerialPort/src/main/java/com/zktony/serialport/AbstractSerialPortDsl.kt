package com.zktony.serialport

import com.zktony.serialport.config.SerialConfig

/**
 * Serial port helper class (abstract class) DSL
 * @param config SerialConfig
 */
inline fun serialPortOf(config: SerialConfig.() -> Unit): AbstractSerialPort {
    val serialConfig = SerialConfig().apply(config)
    return object : AbstractSerialPort(serialConfig) {}
}