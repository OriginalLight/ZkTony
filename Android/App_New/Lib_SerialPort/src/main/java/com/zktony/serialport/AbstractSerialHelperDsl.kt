package com.zktony.serialport

import com.zktony.serialport.config.SerialConfig

inline fun abstractSerialHelperOf(config: SerialConfig.() -> Unit): AbstractSerialHelper {
    val serialConfig = SerialConfig().apply(config)
    return object : AbstractSerialHelper(serialConfig) {}
}