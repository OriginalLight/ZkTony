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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.zktony.android.R
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.IncubationStage
import com.zktony.android.data.entities.internal.IncubationStageStatus
import com.zktony.android.ui.*
import com.zktony.android.ui.navigation.NavigationActions
import com.zktony.android.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.zktony.android.ui.utils.PageType
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
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppBar(
    modifier: Modifier = Modifier,
    uiState: SettingUiState,
    uiEvent: (SettingUiEvent) -> Unit,
    navigation: () -> Unit,
) {
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
                    ElevatedButton(onClick = { uiEvent(SettingUiEvent.Insert) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
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
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramAppBar(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit,
    navigation: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var deleteCount by remember { mutableIntStateOf(0) }

    if (showDialog) {
        InputDialog(
            onConfirm = {
                scope.launch {
                    showDialog = false
                    val nameList = uiState.entities.map { it.displayText }
                    if (nameList.contains(it)) {
                        snackbarHostState.showSnackbar("已存在 $it")
                    } else {
                        uiEvent(ProgramUiEvent.Insert(it))
                    }
                }
            }
        ) { showDialog = false }
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
                    val selected = uiState.entities.find { it.id == uiState.selected } ?: Program()
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
                            showDialog = true
                        } else {
                            val selected =
                                uiState.entities.find { it.id == uiState.selected } ?: Program()
                            val stages = selected.stages.toMutableList()
                            stages.add(IncubationStage())
                            uiEvent(ProgramUiEvent.Update(selected.copy(stages = stages)))
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

                val delete = (uiState.selected != 0L && uiState.page == PageType.PROGRAM_LIST)
                        || (uiState.page == PageType.PROGRAM_DETAIL
                        && uiState.selected != 0L
                        && uiState.entities.find { it.id == uiState.selected }?.stages?.find { it.status == IncubationStageStatus.CURRENT } != null)

                AnimatedVisibility(visible = delete) {
                    ElevatedButton(onClick = {
                        scope.launch {
                            if (deleteCount == 0) {
                                deleteCount++
                                snackbarHostState.showSnackbar("再次点击删除")
                            } else {
                                deleteCount = 0
                                if (uiState.page == PageType.PROGRAM_LIST) {
                                    uiEvent(ProgramUiEvent.Delete(uiState.selected))
                                } else {
                                    val selected =
                                        uiState.entities.find { it.id == uiState.selected }
                                            ?: Program()
                                    val stages = selected.stages.toMutableList()
                                    stages.removeAt(stages.indexOf(stages.find { it.status == IncubationStageStatus.CURRENT }))
                                    uiEvent(ProgramUiEvent.Update(selected.copy(stages = stages)))
                                }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = if (deleteCount == 0) MaterialTheme.colorScheme.primary else Color.Red
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
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurveAppBar(
    modifier: Modifier = Modifier,
    navigation: () -> Unit
) {
    TopAppBar(
        title = {
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