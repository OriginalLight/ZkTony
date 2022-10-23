package com.zktony.www.common.app

import com.zktony.www.ui.home.model.Cmd

sealed class AppState {
    data class SendCmd(val cmd: Cmd) : AppState()
    data class ReceiveCmd(val cmd: Cmd) : AppState()
}
