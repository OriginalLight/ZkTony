package com.zktony.android.ui.components.timeline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.data.entities.IncubationFlow
import com.zktony.android.ui.theme.AppTheme
import java.util.Date

@Composable
internal fun Message(
    hiringStage: IncubationStage,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .align(Alignment.CenterStart),
            colors = CardDefaults.cardColors(containerColor = getBackgroundColor(hiringStage))
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(12.dp),
                text = hiringStage.flows.displayText,
                style = getTextStyle(hiringStage),
                fontWeight = getFontWeight(hiringStage),
                color = getTextColor(hiringStage)
            )
        }
    }
}

@Composable
private fun getBackgroundColor(hiringStage: IncubationStage) = when (hiringStage.status) {
    IncubationStageStatus.UPCOMING -> MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
    IncubationStageStatus.CURRENT -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
    else -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun getTextColor(hiringStage: IncubationStage) =
    if (hiringStage.status == IncubationStageStatus.UPCOMING) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.63f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

@Composable
private fun getFontWeight(hiringStage: IncubationStage) =
    if (hiringStage.status == IncubationStageStatus.CURRENT) {
        W500
    } else {
        W400
    }

@Composable
private fun getTextStyle(hiringStage: IncubationStage) =
    if (hiringStage.status == IncubationStageStatus.CURRENT) {
        MaterialTheme.typography.bodyLarge
    } else {
        MaterialTheme.typography.bodyMedium
    }

@Preview(showBackground = true)
@Composable
private fun MessagePreview() {
    AppTheme {
        Message(
            hiringStage = IncubationStage(
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
        Message(
            hiringStage = IncubationStage(
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
        Message(
            modifier = Modifier,
            hiringStage = IncubationStage(
                date = Date(System.currentTimeMillis()),
                flows = IncubationFlow.Blocking(),
                status = IncubationStageStatus.FINISHED
            )
        )
    }
}