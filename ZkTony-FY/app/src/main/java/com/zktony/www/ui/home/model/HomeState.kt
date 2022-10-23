package com.zktony.www.ui.home.model

import com.zktony.www.model.enum.ModuleEnum

/**
 * @author: 刘贺贺
 * @date: 2022-10-09 15:42
 */
sealed class HomeState {
    data class OnSwitchProgram(val index: Int, val module: ModuleEnum) : HomeState()
    data class OnLoadProgram(val uiState: HomeUiState) : HomeState()
    data class OnButtonChange(val module: ModuleEnum) : HomeState()
    data class OnRestCallBack(val success: Boolean) : HomeState()
    object OnPause : HomeState()
    object OnInsulating : HomeState()
}
