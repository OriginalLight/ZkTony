package com.zktony.www.data.model

/**
 * @author: 刘贺贺
 * @date: 2022-12-15 14:26
 */
/**
 * 电机组
 */
data class MotorUnits(
    val x: Motor = Motor(id = 0),
    val y: Motor = Motor(id = 1),
    val z: Motor = Motor(id = 2),
    val p1: Motor = Motor(id = 3),
    val p2: Motor = Motor(id = 4),
    val p3: Motor = Motor(id = 5),
    val p4: Motor = Motor(id = 6),
    val p5: Motor = Motor(id = 7),
    val cali: Calibration = Calibration(),
) {
    /**
     * 多点运动
     * @param yDv [Float] y轴运动距离
     * @param zDv [Float] z轴运动距离
     */
    fun toMotionHex(yDv: Float, zDv: Float): String {
        val str = StringBuilder()
        str.append("0,")
        str.append((yDv / cali.y * y.pulse()).toInt())
        str.append(",")
        str.append((zDv / cali.z * z.pulse()).toInt())
        str.append(",")
        return str.toString()
    }

    /**
     * 多点运动
     * @param one [Float] 泵一出液量
     * @param two [Float] 泵二出液量
     * @param three [Float] 泵三出液量
     * @param three [Float] 泵四出液量
     * @param three [Float] 泵五出液量
     */
    fun toPumpHex(
        one: Float = 0f,
        two: Float = 0f,
        three: Float = 0f,
        four: Float = 0f,
        five: Float = 0f
    ): Pair<String, String> {
        val hex = StringBuilder()
        val hex1 = StringBuilder()

        hex.append((one * 1000 / cali.p1 * p1.pulse()).toInt())
        hex.append(",")
        hex.append((two * 1000 / cali.p2 * p2.pulse()).toInt())
        hex.append(",")
        hex.append((three * 1000 / cali.p3 * p3.pulse()).toInt())
        hex.append(",")

        hex1.append((four * 1000 / cali.p4 * p4.pulse()).toInt())
        hex1.append(",")
        hex1.append((five * 1000 / cali.p5 * p5.pulse()).toInt())
        hex1.append(",")
        hex1.append("0,")
        return Pair(hex.toString(), hex1.toString())
    }
}