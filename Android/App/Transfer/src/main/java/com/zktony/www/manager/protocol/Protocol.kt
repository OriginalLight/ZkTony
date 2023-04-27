/**
 * 返回电压与真实电压关系y = 22.727x
 * 返回蠕动泵的与每分钟转速关系y=2.08x
 * 真实电流和返回电流关系y=4.608x + 0.011
 */
package com.zktony.www.manager.protocol

import com.zktony.core.ext.float32ToHex4
import com.zktony.core.ext.hex4ToFloat32
import com.zktony.core.ext.hex4ToInt32
import com.zktony.core.ext.hexHighLow
import com.zktony.core.ext.hexToInt8
import com.zktony.core.ext.int32ToHex4
import com.zktony.core.ext.int8ToHex

/**
 * @author 刘贺贺
 */
class Protocol {

    // 电压系数
    val voltageCoefficient = 22.727f

    // 蠕动泵转速系数
    val pumpSpeedCoefficient = 2.08f

    // 电流系数
    val currentCoefficient = 4.608f

    // 电流偏移量
    val currentOffset = 0.011f

    /**
     * 头1
     * 默认16进制 AA
     */
    var head1 = 170

    /**
     * 头2
     * 默认16进制 55
     */
    var head2 = 85

    /**
     * 地址号
     * 默认16进制 01
     */
    var id = 1

    /**
     * 命令标志
     * 发送16进制 01
     * 接受16进制 02
     * 默认16进制 01
     */
    var cmd = 1

    /**
     * 模块X
     * 直流泵
     * 关闭16进制 00
     * 开启16进制 01
     * 默认16进制 00
     */
    var motorX = 0

    /**
     * 模块X
     * 蠕动泵
     * 关闭16进制 00000000
     * 其他代表速度
     * 正负表示方向
     * 默认16进制 00000000
     * 范围 -560 - 560
     */
    var stepMotorX = 0

    /**
     * 模块X
     * 电源模块电压使能
     * 关闭16进制 00
     * 开启16进制 01
     * 默认16进制 00
     */
    var powerENX = 0

    /**
     * 模块X
     * 电源模块自动修正使能
     * 关闭16进制 00
     * 开启16进制 01
     * 自动追踪targetVal
     * 开启之后setVal将失效
     * 默认16进制 00
     */
    var autoX = 0

    /**
     * 模块X
     * 参数调整电压
     * 范围 1.20 - 2.0
     * 数值越小电压越大
     * 需4位16进制高低位
     * 默认16进制 00000000
     */
    var setVoltageX = 0f

    /**
     * 模块X
     * 目标电压
     * 范围 1.20 - 2.0
     * 数值越小电压越大
     * 需4位16进制高低位
     * 默认16进制 00000000
     */
    var targetVoltageX = 0f

    /**
     * 模块X
     * 电源模块
     * 实际电压
     * 数据为采集值
     * 发送为 00000000
     * 返回实际值
     * 需4位16进制高低位
     * 默认16进制 00000000
     */
    var getVoltageX = 0f

    /**
     * 模块x
     * 电源模块
     * 实际电流
     * 数据为采集值
     * 发送为 00000000
     * 返回实际值
     * 默认16进制 00000000
     */
    var getCurrentX = 0f

    /**
     * 模块x
     * 模块插入传感器
     * 未插入 00
     * 插入 01
     * 默认16进制 00
     */
    var inputSensorX = 0

    /**
     * 模块Y
     * 直流泵
     * 关闭16进制 00
     * 开启16进制 01
     * 默认16进制 00
     */
    var motorY = 0

    /**
     * 模块Y
     * 蠕动泵
     * 关闭16进制 00000000
     * 其他代表速度
     * 正负表示方向
     * 默认16进制 00000000
     * 范围 -560 - 560
     */
    var stepMotorY = 0

    /**
     * 模块Y
     * 电源模块电压使能
     * 关闭16进制 00
     * 开启16进制 01
     * 默认16进制 00
     */
    var powerENY = 0

    /**
     * 模块Y
     * 电源模块自动修正使能
     * 关闭16进制 00
     * 开启16进制 01
     * 自动追踪targetVal
     * 开启之后setVal将失效
     * 默认16进制 00
     */
    var autoY = 0

    /**
     * 模块Y
     * 参数调整电压
     * 范围 1.20 - 2.0
     * 数值越小电压越大
     * 需4位16进制高低位
     * 默认16进制 00000000
     */
    var setVoltageY = 0f

    /**
     * 模块Y
     * 目标电压
     * 范围 1.20 - 2.0
     * 数值越小电压越大
     * 需4位16进制高低位
     * 默认16进制 00000000
     */
    var targetVoltageY = 0f

    /**
     * 模块Y
     * 电源模块
     * 实际电压
     * 数据为采集值
     * 发送为 00000000
     * 返回实际值
     * 需4位16进制高低位
     * 默认16进制 00000000
     */
    var getVoltageY = 0f

    /**
     * 模块Y
     * 电源模块
     * 实际电流
     * 数据为采集值
     * 发送为 00000000
     * 返回实际值
     * 默认16进制 00000000
     */
    var getCurrentY = 0f

    /**
     * 模块Y
     * 模块插入传感器
     * 未插入 00
     * 插入 01
     * 默认16进制 00
     */
    var inputSensorY = 0

    /**
     * 水位传感器
     * 未超过警戒线 00
     * 超过警戒线 01
     * 默认16进制 00
     */
    var inputSensorZ = 0

    /**
     * 校验码1
     * 默认16进制 00
     */
    var crc1 = 0

    /**
     * 校验码h
     * 默认16进制 00
     */
    var crch = 0

    /**
     * 结束位1
     * 默认16进制 16
     */
    var end1 = 22

    /**
     * 结束位2
     * 默认16进制 16
     */
    var end2 = 22

    /**
     * 结束位3
     * 默认16进制 16
     */
    var end3 = 22

    /**
     * 结束位4
     * 默认16进制 16
     */
    var end4 = 22

    companion object {
        /**
         * 查询命令
         */
        const val QUERY_HEX = "AA550100000016161616"
    }
}

fun protocol(block: Protocol.() -> Unit): Protocol {
    return Protocol().apply(block)
}

fun Protocol.toHex(): String {
    val hex = StringBuilder()
    hex.append(head1.int8ToHex())
    hex.append(head2.int8ToHex())
    hex.append(id.int8ToHex())
    hex.append(cmd.int8ToHex())
    hex.append(motorX.int8ToHex())
    hex.append((stepMotorX * pumpSpeedCoefficient).toInt().int32ToHex4().hexHighLow())
    hex.append(powerENX.int8ToHex())
    hex.append(autoX.int8ToHex())
    hex.append(setVoltageX.float32ToHex4().hexHighLow())
    hex.append((targetVoltageX / voltageCoefficient).float32ToHex4().hexHighLow())
    hex.append(getVoltageX.float32ToHex4().hexHighLow())
    hex.append(getCurrentX.float32ToHex4().hexHighLow())
    hex.append(inputSensorX.int8ToHex())
    hex.append(motorY.int8ToHex())
    hex.append((stepMotorY * pumpSpeedCoefficient).toInt().int32ToHex4().hexHighLow())
    hex.append(powerENY.int8ToHex())
    hex.append(autoY.int8ToHex())
    hex.append(setVoltageY.float32ToHex4().hexHighLow())
    hex.append((targetVoltageY / voltageCoefficient).float32ToHex4().hexHighLow())
    hex.append(getVoltageY.float32ToHex4().hexHighLow())
    hex.append(getCurrentY.float32ToHex4().hexHighLow())
    hex.append(inputSensorY.int8ToHex())
    hex.append(inputSensorZ.int8ToHex())
    hex.append(crc1.int8ToHex())
    hex.append(crch.int8ToHex())
    hex.append(end1.int8ToHex())
    hex.append(end2.int8ToHex())
    hex.append(end3.int8ToHex())
    hex.append(end4.int8ToHex())
    return hex.toString()
}

fun String.toProtocol(): Protocol {
    /**
     * 返回命令固定118位
     * cmd = 2
     */
    if (this.length != 118) {
        return protocol {  }
    }
    val hex = this
    return protocol {
        head1 = hex.substring(0, 2).hexToInt8()
        head2 = hex.substring(2, 4).hexToInt8()
        id = hex.substring(4, 6).hexToInt8()
        cmd = hex.substring(6, 8).hexToInt8()
        motorX = hex.substring(8, 10).hexToInt8()
        stepMotorX =
            (hex.substring(10, 18).hexHighLow().hex4ToInt32() / pumpSpeedCoefficient).toInt()
        powerENX = hex.substring(18, 20).hexToInt8()
        autoX = hex.substring(20, 22).hexToInt8()
        setVoltageX = hex.substring(22, 30).hexHighLow().hex4ToFloat32()
        targetVoltageX = hex.substring(30, 38).hexHighLow().hex4ToFloat32() * voltageCoefficient
        if (powerENX == 0) {
            getVoltageX = 0f
            getCurrentX = 0f
        } else {
            getVoltageX = hex.substring(38, 46).hexHighLow().hex4ToFloat32() * voltageCoefficient
            getCurrentX = hex.substring(46, 54).hexHighLow()
                .hex4ToFloat32() * currentCoefficient + currentOffset
        }
        inputSensorX = hex.substring(54, 56).hexToInt8()
        motorY = hex.substring(56, 58).hexToInt8()
        stepMotorY =
            (hex.substring(58, 66).hexHighLow().hex4ToInt32() / pumpSpeedCoefficient).toInt()
        powerENY = hex.substring(66, 68).hexToInt8()
        autoY = hex.substring(68, 70).hexToInt8()
        setVoltageY = hex.substring(70, 78).hexHighLow().hex4ToFloat32()
        targetVoltageY = hex.substring(78, 86).hexHighLow().hex4ToFloat32() * voltageCoefficient
        if (powerENY == 0) {
            getVoltageY = 0f
            getCurrentY = 0f
        } else {
            getVoltageY = hex.substring(86, 94).hexHighLow().hex4ToFloat32() * voltageCoefficient
            getCurrentY = hex.substring(94, 102).hexHighLow()
                .hex4ToFloat32() * currentCoefficient + currentOffset
        }
        inputSensorY = hex.substring(102, 104).hexToInt8()
        inputSensorZ = hex.substring(104, 106).hexToInt8()
        crc1 = hex.substring(106, 108).hexToInt8()
        crch = hex.substring(108, 110).hexToInt8()
        end1 = hex.substring(110, 112).hexToInt8()
        end2 = hex.substring(112, 114).hexToInt8()
        end3 = hex.substring(114, 116).hexToInt8()
        end4 = hex.substring(116, 118).hexToInt8()
    }
}