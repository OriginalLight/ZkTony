package com.zktony.www.ui.home.model

import com.zktony.www.R
import com.zktony.www.data.entity.Program
import com.zktony.www.model.state.ButtonState
import com.zktony.www.model.state.ModuleState

/**
 * @author: 刘贺贺
 * @date: 2022-10-09 16:22
 */
data class HomeUiState(
    var programList: List<Program> = emptyList(),
    var moduleA: ModuleState = ModuleState(),
    var moduleB: ModuleState = ModuleState(),
    var moduleC: ModuleState = ModuleState(),
    var moduleD: ModuleState = ModuleState(),
    var btnReset: ButtonState = ButtonState(),
    var btnPause: ButtonState = ButtonState(
        text = "暂停摇床",
        background = R.mipmap.btn_pause,
        textColor = R.color.dark_outline
    ),
    var btnInsulating: ButtonState = ButtonState(text = "抗体保温", textColor = R.color.dark_outline),
)