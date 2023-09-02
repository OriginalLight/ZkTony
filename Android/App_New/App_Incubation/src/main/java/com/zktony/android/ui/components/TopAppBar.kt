package com.zktony.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.entities.Curve
import com.zktony.android.data.entities.History
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.defaults.ProcessDefaults
import com.zktony.android.ui.*
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.extra.Point
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/23 9:03
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    modifier: Modifier = Modifier,
    navigationActions: NavigationActions,
    navigation: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = {
            Image(
                modifier = Modifier.height(48.dp),
                painter = painterResource(id = R.mipmap.logo),
                contentDescription = null
            )
        },
        actions = {
            Row(
                modifier = modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                navigation()
                TOP_LEVEL_DESTINATIONS.forEach { destination ->
                    ElevatedButton(
                        onClick = { navigationActions.navigateTo(destination) },
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = stringResource(id = destination.iconTextId)
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppBar(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    uiEvent: (SettingUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    navigation: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var count by remember { mutableIntStateOf(0) }

    TopAppBar(
        title = {
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 32.dp, vertical = 4.dp),
                text = stringResource(id = R.string.settings),
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                )
            )
        },
        actions = {
            Row(
                modifier = modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(visible = uiState.page == PageType.MOTOR_LIST) {
                    ElevatedButton(onClick = { scope.launch { uiEvent(SettingUiEvent.Insert) } }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                }
                AnimatedVisibility(visible = uiState.selected != 0L && uiState.page == PageType.MOTOR_LIST) {
                    ElevatedButton(onClick = { scope.launch { uiEvent(SettingUiEvent.NavTo(PageType.MOTOR_DETAIL)) } }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                }
                AnimatedVisibility(visible = uiState.selected != 0L && uiState.page == PageType.MOTOR_LIST) {
                    ElevatedButton(onClick = {
                        scope.launch {
                            if (count == 0) {
                                count++
                                snackbarHostState.showSnackbar("再次点击删除")
                            } else {
                                count = 0
                                uiEvent(SettingUiEvent.Delete(uiState.selected))
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = if (count == 0) MaterialTheme.colorScheme.primary else Color.Red
                        )
                    }
                }
                ElevatedButton(onClick = navigation) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramAppBar(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Program>,
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    navigation: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var dialog by remember { mutableStateOf(false) }
    var count by remember { mutableIntStateOf(0) }

    if (dialog) {
        InputDialog(
            onConfirm = {
                scope.launch {
                    uiEvent(ProgramUiEvent.Insert(it))
                    dialog = false
                }
            },
            onCancel = { dialog = false }
        )
    }

    TopAppBar(
        title = {
            if (uiState.page == PageType.PROGRAM_LIST) {
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 32.dp, vertical = 4.dp),
                    text = stringResource(id = R.string.program),
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    )
                )
            } else {
                Column(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 32.dp, vertical = 4.dp)
                ) {
                    val selected =
                        entities.itemSnapshotList.items.find { it.id == uiState.selected }
                            ?: Program()
                    Text(
                        text = selected.displayText,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Italic,
                        )
                    )
                    Text(
                        text = selected.createTime.dateFormat("yyyy/MM/dd"),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                        ),
                        color = Color.Gray,
                    )
                }
            }
        },
        actions = {
            Row(
                modifier = modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ElevatedButton(onClick = {
                    scope.launch {
                        if (uiState.page == PageType.PROGRAM_LIST) {
                            dialog = true
                        } else {
                            val selected =
                                entities.itemSnapshotList.items.find { it.id == uiState.selected }
                                    ?: Program()
                            val processes = selected.processes.toMutableList()
                            processes.add(ProcessDefaults.defaultPrimaryAntibody())
                            uiEvent(ProgramUiEvent.Update(selected.copy(processes = processes)))
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(
                    visible = uiState.page == PageType.PROGRAM_LIST
                            && uiState.selected != 0L
                ) {
                    ElevatedButton(onClick = { uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_DETAIL)) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                }

                AnimatedVisibility(visible = uiState.selected != 0L && uiState.page == PageType.PROGRAM_LIST) {
                    ElevatedButton(onClick = {
                        scope.launch {
                            if (count == 0) {
                                count++
                                snackbarHostState.showSnackbar("再次点击删除")
                            } else {
                                count = 0
                                uiEvent(ProgramUiEvent.Delete(uiState.selected))
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = if (count == 0) MaterialTheme.colorScheme.primary else Color.Red
                        )
                    }
                }

                ElevatedButton(onClick = navigation) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurveAppBar(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Curve>,
    uiState: CurveUiState,
    uiEvent: (CurveUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    navigation: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var count by remember { mutableIntStateOf(0) }
    var dialog by remember { mutableStateOf(false) }

    if (dialog) {
        InputDialog(
            onConfirm = {
                uiEvent(CurveUiEvent.Insert(it))
                dialog = false
            },
            onCancel = { dialog = false }
        )
    }

    TopAppBar(
        title = {
            if (uiState.page == PageType.CURVE_LIST) {
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 32.dp, vertical = 4.dp),
                    text = stringResource(id = R.string.calibration),
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    )
                )
            } else {
                Column(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 32.dp, vertical = 4.dp)
                ) {
                    val selected =
                        entities.itemSnapshotList.items.find { it.id == uiState.selected }
                            ?: Curve(displayText = "None")
                    Text(
                        text = selected.displayText,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Italic,
                        )
                    )
                    Text(
                        text = selected.createTime.dateFormat("yyyy/MM/dd"),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                        ),
                        color = Color.Gray,
                    )
                }
            }
        },
        actions = {
            Row(
                modifier = modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ElevatedButton(onClick = {
                    scope.launch {
                        if (uiState.page == PageType.CURVE_LIST) {
                            dialog = true
                        } else {
                            val selected =
                                entities.itemSnapshotList.items.find { it.id == uiState.selected }
                                    ?: Curve(displayText = "None")
                            val points = selected.points.toMutableList()
                            points.add(Point(0.0, 0.0))
                            uiEvent(CurveUiEvent.Update(selected.copy(points = points)))
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(
                    visible = uiState.page == PageType.CURVE_LIST
                            && uiState.selected != 0L
                ) {
                    ElevatedButton(onClick = { uiEvent(CurveUiEvent.NavTo(PageType.CURVE_DETAIL)) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                }

                AnimatedVisibility(
                    visible = uiState.page == PageType.CURVE_LIST
                            && uiState.selected != 0L
                ) {
                    ElevatedButton(onClick = {
                        scope.launch {
                            if (count == 0) {
                                count++
                                snackbarHostState.showSnackbar("再次点击删除")
                            } else {
                                count = 0
                                uiEvent(CurveUiEvent.Delete(uiState.selected))
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = if (count == 0) MaterialTheme.colorScheme.primary else Color.Red
                        )
                    }
                }

                ElevatedButton(onClick = navigation) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryAppBar(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<History>,
    uiState: HistoryUiState,
    uiEvent: (HistoryUiEvent) -> Unit,
    navigation: () -> Unit
) {
    val scope = rememberCoroutineScope()

    TopAppBar(
        title = {
            val selected =
                entities.itemSnapshotList.items.find { it.id == uiState.selected } ?: History()
            val text = if (uiState.page == PageType.HISTORY_LIST) {
                stringResource(id = R.string.history)
            } else {
                selected.createTime.dateFormat("yyyy/MM/dd")
            }

            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 32.dp, vertical = 4.dp),
                text = text,
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                )
            )
        },
        actions = {
            Row(
                modifier = modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(visible = uiState.page == PageType.HISTORY_DETAIL && uiState.selected != 0L) {
                    ElevatedButton(onClick = {
                        scope.launch {
                            uiEvent(HistoryUiEvent.NavTo(PageType.HISTORY_LIST))
                            uiEvent(HistoryUiEvent.Delete(uiState.selected))
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                }

                ElevatedButton(onClick = navigation) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null
                    )
                }
            }
        }
    )
}