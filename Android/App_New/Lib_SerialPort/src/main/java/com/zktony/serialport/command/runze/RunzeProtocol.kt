package com.zktony.serialport.command.runze

import com.zktony.serialport.ext.checkSumLE

/**
 *
 * 普通命令
 * 帧头 地址码 功能码 参数区 帧尾 和校验
 * B0 B1 B2 B3 B4 B5 B6 B7
 * STX ADDR FUNC 1-8 位 9-16 位 ETX 低字节 高字节
 *
 * 第 1 字节 STX ： 帧头（0xCC）
 * 第 2 字节 ADDR： 从机正常地址（0x00～0x7F）组播地址（0x80～0xFE） 广播地址（0xFF）
 * 第 3 字节 FUNC： 功能码
 * 第 4、5 字节： 功能码对应参数
 * 第 6 字节 ETX： 帧尾（0xDD）
 * 第 7、8 字节： 从字节 1 到 6 的累加和校验码
 *
 * 查询指令
 *
 * 代码 B2       简称                参数说明 B3 B4
 * 0x20         查询地址             地址取值范围是 0x0000～0x007F,默认 00
 * 0x21         查询 RS232 波特率     共 5 种波特率：出厂默认 9600bps B3B4=0x0000 对应的波特率是 9600bps B3B4=0x0001 对应的波特率 19200bps B3B4=0x0002 对应的波特率 38400bps B3B4=0x0003 对应的波特率 57600bps B3B4=0x0004 对应的波特率 115200bp
 * 0x22         查询 RS485 波特率     共 5 种波特率：出厂默认 9600bps B3B4=0x0000 对应的波特率是 9600bps B3B4=0x0001 对应的波特率 19200bps B3B4=0x0002 对应的波特率 38400bps B3B4=0x0003 对应的波特率 57600bps B3B4=0x0004 对应的波特率 115200bps
 * 0x23         查询 CAN 波特率       CAN 波特率对应如下 B3B4=0x0000 100Kbps B3B4=0x0001 200Kbps B3B4=0x0002 500Kbps B3B4=0x0003 1Mbp
 * 0x2E         查询上电自动复位       B3=0x00 B4=0
 * 0x30         查询 CAN 目的地址     B3=0x00 B4=0x00
 * 0x70         查询组播通道 1 地址    B3=0x00 B4=0x00
 * 0x71         查询组播通道 2 地址    B3=0x00 B4=0x00
 * 0x72         查询组播通道 3 地址    B3=0x00 B4=0x00
 * 0x73         查询组播通道 4 地址    B3=0x00 B4=0x00
 * 0x3E         查询当前通道位置       B3=0x00 B4=0x00
 * 0x3F         查询当前版本          B3=0x01 B4=0x09,上述为例子，若查询为上述参数，代表版本为 V1.9,具体见标贴上的版本号
 * 0x4A         查询电机状态          B3=0x00 B4=0x00
 *
 * 阀动作指令
 *
 * 代码 B2         简称                             参数说明 B3 B4
 * 0x44           电机通过码盘转动, 自动选择最优路径     根据切换阀实际通道数而定，例如 10 通道切换阀，则 B3=0xXX B4=0x00 其中 XX 取值范围是 01～0A
 * 0x45           复位                              B3=0x00 B4=0x00切换阀运行到复位光耦处停止
 * 0x4F           原点复位                           B3=0x00 B4=0x00 切换阀运行到编码器原点位置，与 0x45
 * 0xA4           按需求方向切换孔位                   根据切换阀实际通道数而定，参数值不可超过当前阀的最大通道数，且 B3,B4 必须为相邻的两个孔位
 * 0xB4           按需求方向切换到孔位之间              根据切换阀实际通道数而定，参数值不可超过当前阀的最大通道数，且 B3,B4 必须为相邻的两个孔位
 * 0x49           强停                               B3=0x00 B4=0x0
 *
 * 工厂指令
 * 帧头 地址码 功能码 密码 参数区 帧尾 和校验
 * B0 B1 B2 B3,B4,B5,B6 B7 B8 B9 B10 B11 B12 B13
 * STX ADDR FUNC PWD 1-8位 9-16位 17-24位 25-32位 ETX 低字节 高字节
 *
 * 密码 B3 B4 B5 B6
 *
 * B3=0xFF B4=0xEEB5=0xBB B6=0xAA
 *
 * 代码 B2   简称               参数说明 B7 B8 B9 B1
 * 0x00     设定地址            B7=0xXX (B8=0x00 B9=0x00 B10=0x00)其中XX 的取值范围在 V1.9 及以上版本中是 00～7F,V1.9 以下版本是 00～FF,默认 00
 * 0x01     设定 RS232波特率    共 5 种波特率：出厂默认是 9600bps（B8=0x00 B9=0x00 B10=0x00） B7=0x00对应的波特率是 9600bps B7=0x01 对应的波特率 19200bps B7=0x02 对应的波特率 38400bps B7=0x03 对应的波特率 57600bps B7=0x04 对应的波特率 115200bps
 * 0x02     设定 RS485波特率    共 5 种波特率：出厂默认是 9600bps（B8=0x00 B9=0x00 B10=0x00） B7=0x00对应的波特率是 9600bps B7=0x01 对应的波特率 19200bps B7=0x02 对应的波特率 38400bps B7=0x03 对应的波特率 57600bps B7=0x04 对应的波特率 115200bps
 * 0x03     设定 CAN 波特率     共 4 种波特率：出厂默认是 100K（B8=0x00 B9=0x00 B10=0x00）B7=0x00 对应的波特率 100Kbps B7=0x01 对应的波特率 200Kbps B7=0x02 对应的波特率 500Kbps B7=0x03 对应的波特率是 1Mbps
 * 0x0E     设定上电自动复位      B7=0x00 表示非自动复位 B7=0x01 表示自动复位上电自动复位至切换阀 1 号孔和最大孔的中间位置（切换阀出厂默认为自动复位状态）
 * 0x10     设定 CAN 目的地址    B7=0xXX(B8=0x00 B9=0x00 B10=0x00)其中 XX 的取值范围 00～FF,默认是 00
 * 0x50     设定组播通道 1 地址   B7=0xXX(B8=0x00 B9=0x00 B10=0x00)其中 XX 的取值范围 80～FE,默认是 00
 * 0x51     设定组播通道 2 地址   B7=0xXX(B8=0x00 B9=0x00 B10=0x00)其中 XX 的取值范围 80～FE,默认是 00
 * 0x52     设定组播通道 3 地址   B7=0xXX(B8=0x00 B9=0x00 B10=0x00)其中 XX 的取值范围 80～FE,默认是 00
 * 0x53     设定组播通道 4 地址   B7=0xXX(B8=0x00 B9=0x00 B10=0x00)其中 XX 的取值范围 80～FE,默认是 00
 * 0xFC     参数锁定            参数均为 0x00
 * 0xFF     恢复出厂设置         参数均为 0x00
 *
 * 返回指令
 *
 * 帧头 地址码 状态码 参数区 帧尾 和校验
 * B0 B1 B2 B3 B4 B5 B6 B7
 * STX ADDR STATUS 1-8 位 9-16 位 ETX 低字节 高字节
 *
 * 响应参数
 *
 * 代码 B2    说明          其他参数说明=B3 B4
 * 0x00      状态正常       B3=0x00 B4=0x00 例：当使用查询命令“0x3E”时回复 B3 B4 的参数 0x01 0x00～0x0a 0x00，表示多通道 1-10 号通道
 * 0x01      帧错误         参数=0x00 0x00
 * 0x02      参数错误       参数=0x00 0x00
 * 0x03      光耦错误       参数=0x00 0x00
 * 0x04      电机忙         参数=0x00 0x00
 * 0x05      电机堵转       参数=0x00 0x00
 * 0x06      未知位置       参数=0x00 0x00
 * 0xFE      任务挂起       参数=0x00 0x00
 * 0xFF      未知错误       参数=0x00 0x00
 *
 */
class RunzeProtocol {
    var head: Byte = 0xCC.toByte()
    var addr: Byte = 0x00.toByte()
    var funcCode: Byte = 0x00.toByte()
    var data: ByteArray = byteArrayOf()
    var end: Byte = 0xDD.toByte()
    var checksum: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte())

    fun toByteArray(): ByteArray {
        val byteArray = byteArrayOf(head, addr, funcCode)
            .plus(data)
            .plus(end)
        return byteArray.plus(byteArray.checkSumLE())
    }
}

fun ByteArray.toRunzeProtocol(): RunzeProtocol {
    val byteArray = this
    return RunzeProtocol().apply {
        head = byteArray[0]
        addr = byteArray[1]
        funcCode = byteArray[2]
        data = byteArray.copyOfRange(3, byteArray.size - 3)
        end = byteArray[byteArray.size - 3]
        checksum = byteArray.copyOfRange(byteArray.size - 2, byteArray.size)
    }
}