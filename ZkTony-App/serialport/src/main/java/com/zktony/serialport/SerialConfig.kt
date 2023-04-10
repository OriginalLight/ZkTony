package com.zktony.serialport

data class SerialConfig(
    /**
     * 串口索引
     */
    val index: Int = 0,
    /**
     * 串口文件描述符，禁止删除或者重命名
     */
    val device: String = "/dev/ttyS0",

    /**
     * 波特率
     */
    val baudRate: Int = 115200,

    /**
     * 停止位 1 or 2
     */
    val stopBits: Int = 1,

    /**
     * 数据位 5,6,7,8
     */
    val dataBits: Int = 8,

    /**
     * 校验位 0:无校验 1:奇校验 2:偶校验
     */
    val parity: Int = 0,

    /**
     * 流控制 0:无流控制 1:硬件流控制 2:软件流控制
     */
    val flowCon: Int = 0,

    /**
     * 标志位
     */
    val flags: Int = 0,

    /**
     * 发送延时
     */
    val delay: Long = 30L,
)