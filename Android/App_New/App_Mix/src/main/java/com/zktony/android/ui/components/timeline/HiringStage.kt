package com.zktony.android.ui.components.timeline

import java.util.Date

data class HiringStage(
    val date: Date,
    val initiator: MessageSender,
    val status: HiringStageStatus
)