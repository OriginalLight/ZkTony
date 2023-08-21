package com.zktony.android.ui.components.timeline

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.RunCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zktony.android.ui.components.timeline.defaults.CircleParametersDefaults
import com.zktony.android.ui.components.timeline.defaults.LineParametersDefaults

@Composable
fun LazyTimeline(stages: Array<HiringStage>) {
    LazyColumn(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        content = {
            itemsIndexed(stages) { index, stage ->
                TimelineNode(
                    position = mapToTimelineNodePosition(index, stages.size),
                    circleParameters = CircleParametersDefaults.circleParameters(
                        backgroundColor = getIconColor(stage),
                        stroke = getIconStrokeColor(stage),
                        icon = getIcon(stage)
                    ),
                    lineParameters = getLineBrush(
                        circleRadius = 12.dp,
                        index = index,
                        items = stages
                    ),
                    contentStartOffset = 16.dp,
                    spacer = 24.dp
                ) { modifier ->
                    Message(stage, modifier)
                }
            }
        },
        contentPadding = PaddingValues(16.dp)
    )
}

@Composable
private fun getLineBrush(circleRadius: Dp, index: Int, items: Array<HiringStage>): LineParameters? {
    return if (index != items.lastIndex) {
        val currentStage: HiringStage = items[index]
        val nextStage: HiringStage = items[index + 1]
        val circleRadiusInPx = with(LocalDensity.current) { circleRadius.toPx() }
        LineParametersDefaults.linearGradient(
            strokeWidth = 3.dp,
            startColor = (getIconStrokeColor(currentStage)?.color ?: getIconColor(currentStage)),
            endColor = (getIconStrokeColor(nextStage)?.color ?: getIconColor(items[index + 1])),
            startY = circleRadiusInPx * 2
        )
    } else {
        null
    }
}

private fun getIconColor(stage: HiringStage): Color {
    return when (stage.status) {
        HiringStageStatus.FINISHED -> Color.Green
        HiringStageStatus.CURRENT -> Color.Blue
        HiringStageStatus.UPCOMING -> Color.Gray
    }
}

private fun getIconStrokeColor(stage: HiringStage): StrokeParameters? {
    return if (stage.status == HiringStageStatus.UPCOMING) {
        StrokeParameters(color = Color.Gray, width = 2.dp)
    } else {
        null
    }
}

@Composable
private fun getIcon(stage: HiringStage): ImageVector? {
    return when (stage.status) {
        HiringStageStatus.FINISHED -> Icons.Default.Done
        HiringStageStatus.CURRENT -> Icons.Default.RunCircle
        HiringStageStatus.UPCOMING -> null
    }
}

private fun mapToTimelineNodePosition(index: Int, collectionSize: Int) = when (index) {
    0 -> TimelineNodePosition.FIRST
    collectionSize - 1 -> TimelineNodePosition.LAST
    else -> TimelineNodePosition.MIDDLE
}