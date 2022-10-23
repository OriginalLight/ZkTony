package com.zktony.www.common.app

import com.zktony.www.ui.home.model.Cmd

sealed class AppIntent {
    data class SendCmd(val cmd: Cmd) : AppIntent()
    data class ReceiveCmd(val cmd: Cmd) : AppIntent()
}
