package com.zktony.android.ui.components.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.data.entities.IncubationFlow
import com.zktony.android.data.entities.IncubationTag
import com.zktony.android.ui.theme.AppTheme
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StageCard(
    stage: IncubationStage,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = getBackgroundColor(stage = stage),
                    shape = MaterialTheme.shapes.small
                )
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedCard(shape = MaterialTheme.shapes.small) {
                Text(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                    text = stage.flows.displayText,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = getTextColor(stage = stage),
                )
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                maxItemsInEachRow = 3
            ) {
                getParameter(stage = stage).forEach {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = it.second,
                                color = getTextColor(stage = stage)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = it.first,
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize),
                                tint = getTextColor(stage = stage)
                            )
                        })

                }
            }
        }
    }
}

@Composable
private fun getBackgroundColor(stage: IncubationStage) = when (stage.status) {
    IncubationStageStatus.UPCOMING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    IncubationStageStatus.CURRENT -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
    else -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun getTextColor(stage: IncubationStage) =
    if (stage.status == IncubationStageStatus.UPCOMING) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.63f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

private fun getParameter(stage: IncubationStage): List<Pair<ImageVector, String>> {
    val list = mutableListOf<Pair<ImageVector, String>>()
    list.add(Icons.Default.LocalFireDepartment to "${stage.flows.temperature} ℃")
    list.add(
        Icons.Default.LockClock to
                "${stage.flows.duration} ${
                    if (stage.flows.tag == IncubationTag.WASHING) {
                        "Min"
                    } else {
                        "Hour"
                    }
                }"
    )
    list.add(Icons.Default.WaterDrop to "${stage.flows.dosage} μL")
    if (stage.flows is IncubationFlow.PrimaryAntibody) {
        list.add(Icons.Default.Recycling to if (stage.flows.recycle) "Recycle" else "No Recycle")
    }
    if (stage.flows is IncubationFlow.Washing) {
        list.add(Icons.Default.Numbers to "${stage.flows.times} Times")
    }
    return list
}


@Preview(showBackground = true)
@Composable
private fun MessagePreview() {
    AppTheme {
        StageCard(
            stage = IncubationStage(
                date = Date(System.currentTimeMillis()),
                flows = IncubationFlow.Blocking(),
                status = IncubationStageStatus.CURRENT
            ),
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UpcomingStageMessagePreview() {
    AppTheme {
        StageCard(
            stage = IncubationStage(
                date = Date(System.currentTimeMillis()),
                flows = IncubationFlow.Blocking(),
                status = IncubationStageStatus.UPCOMING
            ),
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FinishedMessagePreview() {
    AppTheme {
        StageCard(
            modifier = Modifier,
            stage = IncubationStage(
                date = Date(System.currentTimeMillis()),
                flows = IncubationFlow.PrimaryAntibody(),
                status = IncubationStageStatus.FINISHED
            )
        )
    }
}