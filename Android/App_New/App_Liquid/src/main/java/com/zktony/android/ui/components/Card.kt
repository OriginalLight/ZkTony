package com.zktony.android.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.data.entities.OrificePlate
import com.zktony.android.ui.HomeUiEvent
import com.zktony.android.ui.HomeUiState
import com.zktony.android.ui.JobStatus
import com.zktony.android.utils.extra.format
import com.zktony.android.utils.extra.timeFormat
import kotlinx.coroutines.delay

/**
 * @author 刘贺贺
 * @date 2023/8/10 14:18
 */
@Composable
fun OrificePlateCard(
    modifier: Modifier = Modifier,
    orificePlate: OrificePlate,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.small
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "${orificePlate.column} x ${orificePlate.row}",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                        ),
                    )
                }

            }
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "${
                            orificePlate.orifices.flatten().count { it.selected }
                        }/${orificePlate.orifices.flatten().size}",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                        ),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = if (orificePlate.type == 0) "分液模式" else "混合模式",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                        ),
                    )
                }
            }

            ElevatedCard(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "${orificePlate.delay} ms",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                        ),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = if (orificePlate.type == 0) orificePlate.getVolume()[0].format(1) else "[${
                            orificePlate.getVolume().joinToString(", ") { it.format(1) }
                        }]",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                        ),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobActionCard(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {
    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(key1 = uiState.jobState.status) {
        while (true) {
            when (uiState.jobState.status) {
                JobStatus.RUNNING -> {
                    time += 1
                }

                JobStatus.STOPPED -> {
                    time = 0
                }

                else -> {}
            }
            delay(1000L)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (uiState.jobState.status == JobStatus.STOPPED) {
            ElevatedCard(
                enabled = uiState.loading == 0 && uiState.selected != 0L,
                onClick = { uiEvent(HomeUiEvent.Start) }
            ) {
                Icon(
                    modifier = Modifier.size(128.dp),
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Blue
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Box {
                    LinearProgressIndicator(
                        progress = uiState.jobState.process,
                        modifier = Modifier
                            .width(296.dp)
                            .height(20.dp)
                            .align(Alignment.Center),
                        color = Color.Blue,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp),
                        text = time.timeFormat(),
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                        ),
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ElevatedCard(onClick = {
                        if (uiState.jobState.status == JobStatus.RUNNING) {
                            uiEvent(HomeUiEvent.Pause)
                        } else {
                            uiEvent(HomeUiEvent.Resume)
                        }
                    }) {
                        if (uiState.jobState.status == JobStatus.RUNNING) {
                            Icon(
                                modifier = Modifier.size(128.dp),
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                tint = Color.DarkGray
                            )
                        } else {
                            Icon(
                                modifier = Modifier.size(128.dp),
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Blue
                            )
                        }
                    }

                    ElevatedCard(onClick = { uiEvent(HomeUiEvent.Stop) }) {
                        Icon(
                            modifier = Modifier.size(128.dp),
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}