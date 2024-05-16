package com.zktony.android.ui

import android.media.VolumeShaper.Operation
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.ui.components.HomeAppBar
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.selectedColor
import com.zktony.android.utils.extra.timeFormat
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@Composable
fun HomeRoute(viewModel: HomeViewModel) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    val page by viewModel.page.collectAsStateWithLifecycle()
    val uiFlags by viewModel.uiFlags.collectAsStateWithLifecycle()
    val insulation by viewModel.insulation.collectAsStateWithLifecycle()
    val valve by viewModel.valve.collectAsStateWithLifecycle()
    val motor by viewModel.motor.collectAsStateWithLifecycle()
    val shakerJob by viewModel.shakerJob.collectAsStateWithLifecycle()
    val insulationJob by viewModel.insulationJob.collectAsStateWithLifecycle()
    val valveJob by viewModel.valveJob.collectAsStateWithLifecycle()

    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.HOME -> {}
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = uiFlags) {
        if (uiFlags is UiFlags.Message) {
            snackbarHostState.showSnackbar((uiFlags as UiFlags.Message).message)
            viewModel.dispatch(HomeIntent.Flags(UiFlags.none()))
        }
    }

    Column {
        HomeAppBar(page) { navigation() }
        Box {
            StatusContent(modifier = Modifier.align(Alignment.TopStart), insulation = insulation, valve = valve, motor = motor)
            OperationContent(shakerJob = shakerJob, insulationJob = insulationJob, valveJob = valveJob, dispatch = viewModel::dispatch)
        }
    }
}

@Composable
fun StatusContent(
    modifier: Modifier = Modifier,
    insulation: List<Double>,
    valve: List<Int>,
    motor: List<Int>
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                text = "温控",
                style = MaterialTheme.typography.titleMedium
            )

            insulation.forEach {
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    text = "$it ℃",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                text = "电机",
                style = MaterialTheme.typography.titleMedium
            )

            motor.forEach {
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    text = if (it == 0) "OFF" else "ON",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                text = "阀门",
                style = MaterialTheme.typography.titleMedium
            )

            valve.forEach {
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    text = "$it",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

}

@Composable
fun OperationContent(
    shakerJob: Job?,
    insulationJob: Job?,
    valveJob: Job?,
    dispatch: (HomeIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Box {
                Column(
                    modifier = Modifier
                        .sizeIn(minWidth = 196.dp, minHeight = 96.dp)
                        .background(
                            color = if (shakerJob != null) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            if (shakerJob == null) {
                                dispatch(HomeIntent.Start(0))
                            } else {
                                dispatch(HomeIntent.Stop(0))
                            }
                        }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "摇床测试",
                        style = MaterialTheme.typography.headlineSmall,
                        fontStyle = FontStyle.Italic
                    )
                }

                Text(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset {
                            IntOffset(
                                x = -8.dp.roundToPx(),
                                y = 4.dp.roundToPx()
                            )
                        },
                    text = if (shakerJob != null) {
                        "运行中"
                    } else {
                        "未运行"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic
                )
            }
            Box {
                Column(
                    modifier = Modifier
                        .sizeIn(minWidth = 196.dp, minHeight = 96.dp)
                        .background(
                            color = if (insulationJob != null) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            if (insulationJob == null) {
                                dispatch(HomeIntent.Start(1))
                            } else {
                                dispatch(HomeIntent.Stop(1))
                            }
                        }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "温控测试",
                        style = MaterialTheme.typography.headlineSmall,
                        fontStyle = FontStyle.Italic
                    )
                }

                Text(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset {
                            IntOffset(
                                x = -8.dp.roundToPx(),
                                y = 4.dp.roundToPx()
                            )
                        },
                    text = if (insulationJob != null) {
                        "运行中"
                    } else {
                        "未运行"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic
                )
            }
            Box {
                Column(
                    modifier = Modifier
                        .sizeIn(minWidth = 196.dp, minHeight = 96.dp)
                        .background(
                            color = if (valveJob != null) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            if (valveJob == null) {
                                dispatch(HomeIntent.Start(2))
                            } else {
                                dispatch(HomeIntent.Stop(2))
                            }
                        }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "泵阀测试",
                        style = MaterialTheme.typography.headlineSmall,
                        fontStyle = FontStyle.Italic
                    )
                }

                Text(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset {
                            IntOffset(
                                x = -8.dp.roundToPx(),
                                y = 4.dp.roundToPx()
                            )
                        },
                    text = if (valveJob != null) {
                        "运行中"
                    } else {
                        "未运行"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewStatusContent() {
    StatusContent(
        insulation = listOf(1.0, 2.0, 3.0, 4.0, 5.0),
        valve = listOf(1, 2),
        motor = listOf(1, 2)
    )
}

@Preview
@Composable
fun PreviewOperationContent() {
    OperationContent(
        shakerJob = null,
        insulationJob = null,
        valveJob = null,
        dispatch = {}
    )
}
