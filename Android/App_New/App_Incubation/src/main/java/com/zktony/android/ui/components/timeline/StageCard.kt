package com.zktony.android.ui.components.timeline

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R
import com.zktony.android.data.entities.internal.IncubationStage
import com.zktony.android.data.entities.internal.IncubationStageStatus
import com.zktony.android.data.entities.internal.IncubationTag
import com.zktony.android.ui.theme.AppTheme

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun StageCard(
    stage: IncubationStage,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = getBackgroundColor(stage = stage),
            ),
            shape = MaterialTheme.shapes.small,
            onClick = onClick
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ElevatedCard(shape = MaterialTheme.shapes.small) {
                    Text(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                        text = getDisplayText(stage = stage),
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
                        Row(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.Gray,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = it.first,
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize),
                                tint = getTextColor(stage = stage)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = it.second,
                                color = getTextColor(stage = stage)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getDisplayText(stage: IncubationStage): String {
    return when (stage.tag) {
        IncubationTag.BLOCKING -> stringResource(id = R.string.blocking)
        IncubationTag.PRIMARY_ANTIBODY -> stringResource(id = R.string.primary_antibody)
        IncubationTag.SECONDARY_ANTIBODY -> stringResource(id = R.string.secondary_antibody)
        IncubationTag.WASHING -> stringResource(id = R.string.washing)
        IncubationTag.PHOSPHATE_BUFFERED_SALINE -> stringResource(id = R.string.phosphate_buffered_saline)
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
    list.add(Icons.Default.LocalFireDepartment to "${stage.temperature} ℃")
    list.add(
        Icons.Default.LockClock to
                "${stage.duration} ${
                    if (stage.tag == IncubationTag.WASHING) {
                        "Min"
                    } else {
                        "Hour"
                    }
                }"
    )
    list.add(Icons.Default.WaterDrop to "${stage.dosage} μL")
    if (stage.tag == IncubationTag.PRIMARY_ANTIBODY) {
        list.add(Icons.Default.Recycling to if (stage.recycle) "Recycle" else "No Recycle")
        list.add(Icons.Default.TripOrigin to "${'A' + stage.origin}")
    }
    if (stage.tag == IncubationTag.SECONDARY_ANTIBODY) {
        list.add(Icons.Default.TripOrigin to "${'A' + stage.origin}")
    }
    if (stage.tag == IncubationTag.WASHING) {
        list.add(Icons.Default.Numbers to "${stage.times} Times")
    }
    return list
}


@Preview(showBackground = true)
@Composable
private fun MessagePreview() {
    AppTheme {
        StageCard(
            stage = IncubationStage(
                tag = IncubationTag.BLOCKING,
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
                tag = IncubationTag.PRIMARY_ANTIBODY,
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
                tag = IncubationTag.BLOCKING,
                status = IncubationStageStatus.FINISHED
            )
        )
    }
}