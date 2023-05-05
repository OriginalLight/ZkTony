package com.zktony.serialport.config

data class SerialConfig(
    /**
     * 串口索引
     */
    var index: Int = 0,
    /**
     * 串口文件描述符，禁止删除或者重命名
     */
    var device: String = "/dev/ttyS0",

    /**
     * 波特率
     */
    var baudRate: Int = 115200,

    /**
     * 停止位 1 or 2
     */
    var stopBits: Int = 1,

    /**
     * 数据位 5,6,7,8
     */
    var dataBits: Int = 8,

    /**
     * 校验位 0:无校验 1:奇校验 2:偶校验
     */
    var parity: Int = 0,

    /**
     * 流控制 0:无流控制 1:硬件流控制 2:软件流控制
     */
    var flowCon: Int = 0,

    /**
     * 标志位
     */
    var flags: Int = 0,

    /**
     * 发送延时
     */
    var delay: Long = 30L,

    /**
     * CMD_SHELL
     */
    var cmdSuShell: CmdSuShell = CmdSuShell.CMD_X_BIN_SU_SHELL,

    /**
     * CRC16
     */
    var crc16: Boolean = false,

    /**
     * split
     */
    var split: SPLIT = SPLIT.V1,
)

fun serialConfig(block: SerialConfig.() -> Unit): SerialConfig {
    return SerialConfig().apply(block)
}

enum class CmdSuShell {
    CMD_BIN_SU_SHELL,
    CMD_X_BIN_SU_SHELL,
}

enum class SPLIT {
    V1,
    V2,
}