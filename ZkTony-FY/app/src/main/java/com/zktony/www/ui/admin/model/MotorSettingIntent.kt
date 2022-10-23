package com.zktony.www.ui.admin.model

import com.zktony.www.data.entity.Motor

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 13:33
 */
sealed class MotorSettingIntent {
    data class OnMotorValueChange(val motor: Motor) : MotorSettingIntent()
    data class OnEditMotor(val motor: Motor) : MotorSettingIntent()
    object OnUpdateMotor : MotorSettingIntent()
}
