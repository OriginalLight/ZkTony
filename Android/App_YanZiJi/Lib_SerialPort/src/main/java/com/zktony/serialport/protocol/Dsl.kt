package com.zktony.serialport.protocol

/**
 * Protocol DSL
 * @param invoke Function1<ZktyProtocol, Unit>
 * @return ByteArray
 */
fun zktyProtocolOf(invoke: ZktyProtocol.() -> Unit): ByteArray {
    return ZktyProtocol().apply { invoke() }.toByteArray()
}

/**
 * Modbus RTU Protocol DSL
 * @param invoke Function1<RtuProtocol, Unit>
 * @return ByteArray
 */
fun rtuProtocolOf(invoke: RtuProtocol.() -> Unit): ByteArray {
    return RtuProtocol().apply { invoke() }.toByteArray()
}

/**
 * Runze Protocol DSL
 * @param invoke Function1<RunzeProtocol, Unit>
 * @return ByteArray
 */
fun runzeProtocolOf(invoke: RunzeProtocol.() -> Unit): ByteArray {
    return RunzeProtocol().apply { invoke() }.toByteArray()
}