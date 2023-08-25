package com.zktony.android.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.data.entities.OrificePlate
import com.zktony.android.ui.RuntimeAction
import com.zktony.android.ui.RuntimeStatus
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
fun RuntimeCard(
    modifier: Modifier = Modifier,
    status: RuntimeStatus = RuntimeStatus.STOPPED,
    process: Float = 0f,
    uiEvent: (RuntimeAction) -> Unit = {},
) {
    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(key1 = status) {
        while (true) {
            when (status) {
                RuntimeStatus.RUNNING -> {
                    time += 1
                }

                RuntimeStatus.STOPPED -> {
                    time = 0
                }

                else -> {}
            }
            delay(1000L)
        }
    }

    ElevatedCard(shape = MaterialTheme.shapes.small) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = time.timeFormat(),
                            style = TextStyle(
                                fontSize = 50.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontStyle = FontStyle.Italic,
                            ),
                        )
                        if (status == RuntimeStatus.RUNNING) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(32.dp)
                                    .offset {
                                        IntOffset(-8, 8)
                                    },
                                strokeWidth = 4.dp,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = status.name,
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontStyle = FontStyle.Italic,
                                color = when (status) {
                                    RuntimeStatus.STOPPED -> Color.Gray
                                    RuntimeStatus.RUNNING -> Color.Green
                                    RuntimeStatus.PAUSED -> Color.Yellow
                                    RuntimeStatus.ERROR -> Color.Red
                                }
                            ),
                        )
                    }

                }
                Card(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "${process * 100}%",
                            style = TextStyle(
                                fontSize = 24.sp,
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
                if (status == RuntimeStatus.STOPPED) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .animateContentSize(),
                        onClick = { uiEvent(RuntimeAction.START) }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Icon(
                                modifier = Modifier
                                    .size(96.dp)
                                    .align(Alignment.Center),
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                if (status != RuntimeStatus.STOPPED) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .animateContentSize(),
                        onClick = {
                            if (status == RuntimeStatus.PAUSED) uiEvent(RuntimeAction.RESUME)
                            else uiEvent(RuntimeAction.PAUSE)
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Icon(
                                modifier = Modifier
                                    .size(96.dp)
                                    .align(Alignment.Center),
                                imageVector = if (status == RuntimeStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                tint = if (status == RuntimeStatus.PAUSED) MaterialTheme.colorScheme.primary else Color.Yellow,
                            )
                        }
                    }
                }

                if (status != RuntimeStatus.STOPPED) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .animateContentSize(),
                        onClick = { uiEvent(RuntimeAction.STOP) }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Icon(
                                modifier = Modifier
                                    .size(96.dp)
                                    .align(Alignment.Center),
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                tint = Color.Red,
                            )
                        }
                    }
                }
            }
        }
    }
}