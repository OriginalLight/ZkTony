package com.zktony.www.ui.admin.model

import com.zktony.www.data.entity.Motor

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 13:36
 */
sealed class MotorSettingState {
    data class OnDataBaseChange(val motorList: List<Motor>) : MotorSettingState()
    data class OnUpdateMessage(val message: String) : MotorSettingState()
    data class OnMotorValueChange(val motor: Motor) : MotorSettingState()
}