package com.zktony.www.model

import com.zktony.www.data.entity.Motor

/**
 * @author: 刘贺贺
 * @date: 2022-10-18 15:01
 */
data class PumpMotor(
    var pumpOne: Motor = Motor(),
    var pumpTwo: Motor = Motor(),
    var pumpThree: Motor = Motor(),
    var pumpFour: Motor = Motor(),
    var pumpFive: Motor = Motor(),
    // 泵一转一圈的出液量
    var volumeOne: Float = 0.5f,
    // 泵二转一圈的出液量
    var volumeTwo: Float = 0.5f,
    // 泵三转一圈的出液量
    var volumeThree: Float = 0.5f,
    // 泵四转一圈的出液量
    var volumeFour: Float = 0.5f,
    // 泵五转一圈的出液量
    var volumeFive: Float = 0.5f,
) {

    /**
     *  电机转一圈需要的脉冲数
     */
    private fun pulseCount(motor: Motor): Int {
        return 200 * motor.subdivision
    }

    /**
     * 泵一出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    fun pumpOneVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpOne) / this.volumeOne).toInt().toString()
    }

    /**
     * 泵二出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    fun pumpTwoVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpTwo) / this.volumeTwo).toInt().toString()
    }

    /**
     * 泵三出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    fun pumpThreeVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpThree) / this.volumeThree).toInt().toString()
    }

    /**
     * 泵四出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    fun pumpFourVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpFour) / this.volumeFour).toInt().toString()
    }

    /**
     * 泵五出任意量液所需要的脉冲数
     * @param volume [Float] 出液量
     * @return [String] 脉冲数
     */
    fun pumpFiveVolumePulseCount(volume: Float): String {
        return (volume * pulseCount(pumpFive) / this.volumeFive).toInt().toString()
    }
}