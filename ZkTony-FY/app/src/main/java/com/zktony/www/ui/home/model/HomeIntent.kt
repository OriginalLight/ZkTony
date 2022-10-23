package com.zktony.www.ui.home.model

import com.zktony.www.model.enum.ModuleEnum


/**
 * @author: 刘贺贺
 * @date: 2022-10-09 15:41
 */
sealed class HomeIntent {
    data class OnSwitchProgram(val index: Int, val module: ModuleEnum) : HomeIntent()
    data class OnStart(val module: ModuleEnum) : HomeIntent()
    data class OnStop(val module: ModuleEnum) : HomeIntent()
    object OnReset : HomeIntent()
    object OnPause : HomeIntent()
    object OnInsulating : HomeIntent()
}
