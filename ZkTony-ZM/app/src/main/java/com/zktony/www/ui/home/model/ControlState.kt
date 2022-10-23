package com.zktony.www.ui.home.model

import com.zktony.www.ui.home.model.Model.A
import com.zktony.www.ui.home.model.Model.B

/**
 * @author 刘贺贺
 */
class ControlState {
    var modelX = A
    var modelY = A
    var motorX = 0
    var motorY = 0
    var voltageX = 0f
    var voltageY = 0f
    var timeX = 0
    var timeY = 0
    var isRunX = false
    var isRunY = false
    var stepMotorX = 0
    var stepMotorY = 0
    val isCanStartX: Boolean
        get() {
            if (modelX === A) {
                return motorX > 0 && voltageX > 0 && timeX > 0
            }
            return if (modelX === B) {
                voltageX > 0 && timeX > 0
            } else false
        }
    val isCanStartY: Boolean
        get() {
            if (modelY === A) {
                return motorY > 0 && voltageY > 0 && timeY > 0
            }
            return if (modelY === B) {
                voltageY > 0 && timeY > 0
            } else false
        }
}