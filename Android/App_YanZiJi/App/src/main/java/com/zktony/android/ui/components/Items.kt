package com.zktony.android.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R
import com.zktony.android.ui.utils.selectedColor
import com.zktony.android.utils.extra.dateFormat
import com.zktony.room.entities.Calibration
import com.zktony.room.entities.History
import com.zktony.room.entities.Motor
import com.zktony.room.entities.Program
import com.zktony.room.entities.internal.IncubationStage
import com.zktony.room.entities.internal.Log
import com.zktony.room.entities.internal.Point
import kotlinx.coroutines.launch

@Composable
fun HistoryItem(
    index: Int,
    item: History,
    onClick: (History) -> Unit,
) {
    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick(item) },
        headlineContent = {
            Text(
                text = item.createTime.dateFormat("yyyy/MM/dd"),
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(
                text = item.createTime.dateFormat("HH:mm:ss"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        },
        leadingContent = {
            Text(
                text = "${index + 1}、",
                style = MaterialTheme.typography.headlineSmall,
                fontStyle = FontStyle.Italic
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
fun LogItem(item: Log) {

    ListItem(
        modifier = Modifier.clip(MaterialTheme.shapes.small),
        headlineContent = {
            Text(
                text = item.createTime.dateFormat("HH:mm:ss"),
                style = MaterialTheme.typography.bodySmall
            )
        },
        supportingContent = {
            Text(
                modifier = Modifier.padding(8.dp),
                text = item.message,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Text(
                text = item.level,
                style = MaterialTheme.typography.bodyLarge,
                color = when (item.level) {
                    "DEBUG" -> Color.Blue
                    "INFO" -> Color.Green
                    "WARN" -> Color.Yellow
                    "ERROR" -> Color.Red
                    else -> Color.Gray
                }
            )
        },
        trailingContent = {
            Text(text = "${'A' + item.index}", style = MaterialTheme.typography.headlineMedium)
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgramItem(
    index: Int,
    item: Program,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onRename: () -> Unit
) {
    var longClick by remember(item) { mutableStateOf(false) }
    var delete by remember(item) { mutableIntStateOf(0) }

    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { longClick = true },
            ),
        headlineContent = {
            Text(
                text = item.displayText,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = item.createTime.dateFormat("yyyy/MM/dd"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        },
        leadingContent = {
            Text(
                text = "${index + 1}、",
                style = MaterialTheme.typography.headlineSmall,
                fontStyle = FontStyle.Italic
            )
        },
        trailingContent = {
            if (longClick) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = {
                        if (delete == 0) {
                            delete = 1
                        } else {
                            onDelete()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = if (delete == 1) Color.Red else MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.FileCopy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onRename) {
                        Icon(
                            imageVector = Icons.Default.DriveFileRenameOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncubationStageItem(
    stage: IncubationStage,
    selected: Boolean,
    onClick: (Int) -> Unit
) {

    val displayText = when (stage.type) {
        0 -> stringResource(id = R.string.blocking)
        1 -> stringResource(id = R.string.primary_antibody)
        2 -> stringResource(id = R.string.secondary_antibody)
        3 -> stringResource(id = R.string.washing)
        4 -> stringResource(id = R.string.phosphate_buffered_saline)
        else -> "未知"
    }

    val info: List<String> = mutableListOf<String>().apply {
        add("${stage.temperature} ℃")
        add("${stage.dosage} 微升")

        if (stage.type != 4) {
            add("${stage.duration} ${if (stage.type == 3) "分钟" else "小时"}")
        }

        if (stage.type == 1 || stage.type == 2) {
            add("${'@' + stage.origin}")
            add(if (stage.recycle) "回收" else "不回收")
        }

        if (stage.type == 3) {
            add("${stage.times} 次")
        }
    }

    Column(
        modifier = Modifier
            .background(
                color = selectedColor(selected),
                shape = MaterialTheme.shapes.small
            )
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick(0) }
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onClick(1) }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = " ${stringResource(id = R.string.up)} ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onClick(2) }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = " ${stringResource(id = R.string.down)} ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onClick(3) }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = stringResource(id = R.string.delete),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
        }
        HorizontalDivider()
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            info.forEach { text ->
                Text(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    text = text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncubationStageItem(item: IncubationStage) {

    val displayText = when (item.type) {
        0 -> stringResource(id = R.string.blocking)
        1 -> stringResource(id = R.string.primary_antibody)
        2 -> stringResource(id = R.string.secondary_antibody)
        3 -> stringResource(id = R.string.washing)
        4 -> stringResource(id = R.string.phosphate_buffered_saline)
        else -> "未知"
    }

    val info: List<String> = mutableListOf<String>().apply {
        add("${item.temperature} ℃")
        add("${item.dosage} 微升")

        if (item.type != 4) {
            add("${item.duration} ${if (item.type == 3) "分钟" else "小时"}")
        }

        if (item.type == 1) {
            add("${'@' + item.origin}")
            add(if (item.recycle) "回收" else "不回收")
        }

        if (item.type == 2) {
            add("${'@' + item.origin}")
        }

        if (item.type == 3) {
            add("${item.times} 次")
        }
    }

    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = when (item.flags) {
                    0 -> "已完成"
                    1 -> "进行中"
                    2 -> "未开始"
                    else -> "未知"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (item.flags) {
                    0 -> Color.Gray
                    1 -> Color.Green
                    2 -> Color.Blue
                    else -> Color.Red
                }
            )
        }
        HorizontalDivider()
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            info.forEach { text ->
                Text(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    text = text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MotorItem(
    item: Motor,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var delete by remember(item) { mutableStateOf(false) }

    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { delete = true },
            ),
        headlineContent = {
            Text(
                text = item.displayText,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        supportingContent = {
            Text(
                text = "ADS - ${item.acceleration}/${item.deceleration}/${item.speed}",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = {
            Text(
                text = "M ${item.index}",
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        trailingContent = {
            if (delete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}