package com.zktony.android.ui.components.timeline

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.NextPlan
import androidx.compose.material.icons.filled.RunCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zktony.android.data.entities.IncubationFlow
import com.zktony.android.ui.components.timeline.defaults.CircleParametersDefaults
import com.zktony.android.ui.components.timeline.defaults.LineParametersDefaults
import com.zktony.android.ui.theme.AppTheme
import java.util.Date

@Composable
fun LazyTimeline(
    modifier: Modifier = Modifier,
    stages: Array<IncubationStage>
) {
    LazyColumn(
        modifier = modifier,
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
                    StageCard(stage, modifier)
                }
            }
        },
        contentPadding = PaddingValues(16.dp)
    )
}

@Composable
private fun getLineBrush(
    circleRadius: Dp,
    index: Int,
    items: Array<IncubationStage>
): LineParameters? {
    return if (index != items.lastIndex) {
        val currentStage: IncubationStage = items[index]
        val nextStage: IncubationStage = items[index + 1]
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

private fun getIconColor(stage: IncubationStage): Color {
    return when (stage.status) {
        IncubationStageStatus.FINISHED -> Color.Green
        IncubationStageStatus.CURRENT -> Color.Blue
        IncubationStageStatus.UPCOMING -> Color.Gray
    }
}

private fun getIconStrokeColor(stage: IncubationStage): StrokeParameters? {
    return if (stage.status == IncubationStageStatus.UPCOMING) {
        StrokeParameters(color = Color.Gray, width = 2.dp)
    } else {
        null
    }
}

@Composable
private fun getIcon(stage: IncubationStage): ImageVector {
    return when (stage.status) {
        IncubationStageStatus.FINISHED -> Icons.Default.Done
        IncubationStageStatus.CURRENT -> Icons.Default.RunCircle
        IncubationStageStatus.UPCOMING -> Icons.Default.NextPlan
    }
}

private fun mapToTimelineNodePosition(index: Int, collectionSize: Int) = when (index) {
    0 -> TimelineNodePosition.FIRST
    collectionSize - 1 -> TimelineNodePosition.LAST
    else -> TimelineNodePosition.MIDDLE
}


@Composable
@Preview(showBackground = true)
fun LazyTimelinePreview() {
    AppTheme {
        LazyTimeline(
            stages = arrayOf(
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
                )
            )
        )
    }
}