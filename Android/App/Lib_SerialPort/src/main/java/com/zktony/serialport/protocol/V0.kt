/**
 * 返回电压与真实电压关系y = 22.727x
 * 返回蠕动泵的与每分钟转速关系y=2.08x
 * 真实电流和返回电流关系y=4.608x + 0.011
 */
package com.zktony.serialport.protocol

import com.zktony.serialport.ext.floatToHex
import com.zktony.serialport.ext.hexToFloat
import com.zktony.serialport.ext.hexToInt
import com.zktony.serialport.ext.hexHighLow
import com.zktony.serialport.ext.intToHex

/**
 * @author 刘贺贺
 */
class V0 {

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
     * 校验码low
     * 默认16进制 00
     */
    var crcl = 0

    /**
     * 校验码high
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

fun v0(block: V0.() -> Unit): V0 {
    return V0().apply(block)
}

fun V0.toHex(): String {
    val hex = StringBuilder()
    hex.append(head1.intToHex())
    hex.append(head2.intToHex())
    hex.append(id.intToHex())
    hex.append(cmd.intToHex())
    hex.append(motorX.intToHex())
    hex.append((stepMotorX * pumpSpeedCoefficient).toInt().intToHex(4).hexHighLow())
    hex.append(powerENX.intToHex())
    hex.append(autoX.intToHex())
    hex.append(setVoltageX.floatToHex().hexHighLow())
    hex.append((targetVoltageX / voltageCoefficient).floatToHex().hexHighLow())
    hex.append(getVoltageX.floatToHex().hexHighLow())
    hex.append(getCurrentX.floatToHex().hexHighLow())
    hex.append(inputSensorX.intToHex())
    hex.append(motorY.intToHex())
    hex.append((stepMotorY * pumpSpeedCoefficient).toInt().intToHex(4).hexHighLow())
    hex.append(powerENY.intToHex())
    hex.append(autoY.intToHex())
    hex.append(setVoltageY.floatToHex().hexHighLow())
    hex.append((targetVoltageY / voltageCoefficient).floatToHex().hexHighLow())
    hex.append(getVoltageY.floatToHex().hexHighLow())
    hex.append(getCurrentY.floatToHex().hexHighLow())
    hex.append(inputSensorY.intToHex())
    hex.append(inputSensorZ.intToHex())
    hex.append(crcl.intToHex())
    hex.append(crch.intToHex())
    hex.append(end1.intToHex())
    hex.append(end2.intToHex())
    hex.append(end3.intToHex())
    hex.append(end4.intToHex())
    return hex.toString()
}

fun String.toV0(): V0 {
    /**
     * 返回命令固定118位
     * cmd = 2
     */
    if (this.length != 118) {
        return v0 {  }
    }
    val hex = this
    return v0 {
        head1 = hex.substring(0, 2).hexToInt()
        head2 = hex.substring(2, 4).hexToInt()
        id = hex.substring(4, 6).hexToInt()
        cmd = hex.substring(6, 8).hexToInt()
        motorX = hex.substring(8, 10).hexToInt()
        stepMotorX =
            (hex.substring(10, 18).hexHighLow().hexToInt() / pumpSpeedCoefficient).toInt()
        powerENX = hex.substring(18, 20).hexToInt()
        autoX = hex.substring(20, 22).hexToInt()
        setVoltageX = hex.substring(22, 30).hexHighLow().hexToFloat()
        targetVoltageX = hex.substring(30, 38).hexHighLow().hexToFloat() * voltageCoefficient
        if (powerENX == 0) {
            getVoltageX = 0f
            getCurrentX = 0f
        } else {
            getVoltageX = hex.substring(38, 46).hexHighLow().hexToFloat() * voltageCoefficient
            getCurrentX = hex.substring(46, 54).hexHighLow()
                .hexToFloat() * currentCoefficient + currentOffset
        }
        inputSensorX = hex.substring(54, 56).hexToInt()
        motorY = hex.substring(56, 58).hexToInt()
        stepMotorY =
            (hex.substring(58, 66).hexHighLow().hexToInt() / pumpSpeedCoefficient).toInt()
        powerENY = hex.substring(66, 68).hexToInt()
        autoY = hex.substring(68, 70).hexToInt()
        setVoltageY = hex.substring(70, 78).hexHighLow().hexToFloat()
        targetVoltageY = hex.substring(78, 86).hexHighLow().hexToFloat() * voltageCoefficient
        if (powerENY == 0) {
            getVoltageY = 0f
            getCurrentY = 0f
        } else {
            getVoltageY = hex.substring(86, 94).hexHighLow().hexToFloat() * voltageCoefficient
            getCurrentY = hex.substring(94, 102).hexHighLow()
                .hexToFloat() * currentCoefficient + currentOffset
        }
        inputSensorY = hex.substring(102, 104).hexToInt()
        inputSensorZ = hex.substring(104, 106).hexToInt()
        crcl = hex.substring(106, 108).hexToInt()
        crch = hex.substring(108, 110).hexToInt()
        end1 = hex.substring(110, 112).hexToInt()
        end2 = hex.substring(112, 114).hexToInt()
        end3 = hex.substring(114, 116).hexToInt()
        end4 = hex.substring(116, 118).hexToInt()
    }
}