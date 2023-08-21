package com.zktony.android.ui.components.timeline

import com.zktony.android.data.entities.IncubationFlow
import java.util.Date

data class IncubationStage(
    val date: Date,
    val flows: IncubationFlow,
    val status: IncubationStageStatus
)