package com.zktony.android.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zktony.android.data.entities.IncubationFlow
import com.zktony.android.ui.components.timeline.IncubationStage
import com.zktony.android.ui.components.timeline.IncubationStageStatus
import com.zktony.android.ui.components.timeline.LazyTimeline
import java.util.Date

/**
 * @author 刘贺贺
 * @date 2023/8/21 16:52
 */

@Composable
fun Test() {
    Row(modifier = Modifier.fillMaxSize()) {
        val stages = arrayOf(
            IncubationStage(
                date = Date(),
                status = IncubationStageStatus.FINISHED,
                flows = IncubationFlow.Blocking()
            ),
            IncubationStage(
                date = Date(),
                status = IncubationStageStatus.FINISHED,
                flows = IncubationFlow.Washing()
            ),
            IncubationStage(
                date = Date(),
                status = IncubationStageStatus.FINISHED,
                flows = IncubationFlow.PrimaryAntibody()
            ),
            IncubationStage(
                date = Date(),
                status = IncubationStageStatus.CURRENT,
                flows = IncubationFlow.Washing()
            ),
            IncubationStage(
                date = Date(),
                status = IncubationStageStatus.UPCOMING,
                flows = IncubationFlow.SecondaryAntibody()
            ),
            IncubationStage(
                date = Date(),
                status = IncubationStageStatus.UPCOMING,
                flows = IncubationFlow.Washing()
            ),
            IncubationStage(
                date = Date(),
                status = IncubationStageStatus.UPCOMING,
                flows = IncubationFlow.PhosphateBufferedSaline()
            ),
        )

        LazyTimeline(
            modifier = Modifier.fillMaxWidth(0.5f),
            stages = stages
        )
    }
}