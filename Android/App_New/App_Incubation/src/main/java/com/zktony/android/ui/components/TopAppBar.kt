package com.zktony.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R
import com.zktony.android.data.entities.IncubationStage
import com.zktony.android.data.entities.IncubationStageStatus
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.CalibrationUiEvent
import com.zktony.android.ui.CalibrationUiState
import com.zktony.android.ui.ProgramUiEvent
import com.zktony.android.ui.ProgramUiState
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
    actions: @Composable () -> Unit = {},
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
                text = stringResource(id = R.string.tab_setting),
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
                actions()
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
                    val nameList = uiState.entities.map { it.text }
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
                    text = stringResource(id = R.string.tab_program),
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
                        text = selected.text,
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


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CalibrationAppBar(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState,
    uiEvent: (CalibrationUiEvent) -> Unit,
    navigation: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var deleteCount by remember { mutableIntStateOf(0) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var volume by remember { mutableStateOf("") }
    val softKeyboard = LocalSoftwareKeyboardController.current

    if (showDialog) {
        InputDialog(
            onConfirm = {
                scope.launch {
                    showDialog = false
                    val nameList = uiState.entities.map { it.text }
                    if (nameList.contains(it)) {
                        snackbarHostState.showSnackbar("已存在 $it")
                    } else {
                        uiEvent(CalibrationUiEvent.Insert(it))
                    }
                }
            }
        ) { showDialog = false }
    }

    val height = if (uiState.page == PageType.CALIBRATION_LIST) 64.dp else 128.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (uiState.page == PageType.CALIBRATION_LIST) {
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 32.dp, vertical = 4.dp),
                text = stringResource(id = R.string.tab_calibration),
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                )
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            TextField(
                modifier = Modifier.weight(1f),
                value = TextFieldValue(volume, TextRange(volume.length)),
                onValueChange = { volume = it.text },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.dosage),
                        fontStyle = FontStyle.Italic,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                    )
                },

                leadingIcon = {
                    CircleTabRow(
                        modifier = Modifier.width(500.dp),
                        tabItems = listOf("M0", "M1", "M2", "M3", "M4", "M5"),
                        selected = selectedTabIndex,
                    ) {
                        scope.launch {
                            selectedTabIndex = it
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        softKeyboard?.hide()
                    }
                ),
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                textStyle = TextStyle(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                ),
            )
        }

        Row(
            modifier = modifier
                .padding(vertical = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(visible = uiState.page == PageType.CALIBRATION_LIST) {
                ElevatedButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.page == PageType.CALIBRATION_LIST
                        && uiState.selected != 0L
            ) {
                ElevatedButton(onClick = { uiEvent(CalibrationUiEvent.NavTo(PageType.CALIBRATION_DETAIL)) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.page == PageType.CALIBRATION_LIST
                        && uiState.selected != 0L
            ) {
                ElevatedButton(onClick = { uiEvent(CalibrationUiEvent.Active(uiState.selected)) }) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.page == PageType.CALIBRATION_LIST
                        && uiState.selected != 0L
            ) {
                ElevatedButton(onClick = {
                    scope.launch {
                        if (deleteCount == 0) {
                            deleteCount++
                            snackbarHostState.showSnackbar("再次点击删除")
                        } else {
                            deleteCount = 0
                            uiEvent(CalibrationUiEvent.Delete(uiState.selected))
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

            AnimatedVisibility(visible = uiState.page == PageType.CALIBRATION_DETAIL) {
                ElevatedButton(onClick = {
                    scope.launch {
                        if (uiState.loading == 0) {
                            uiEvent(CalibrationUiEvent.AddLiquid(selectedTabIndex))
                        }
                    }
                }) {
                    if (uiState.loading == 0) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            AnimatedVisibility(visible = uiState.page == PageType.CALIBRATION_DETAIL && volume.isNotEmpty()) {
                ElevatedButton(onClick = {
                    scope.launch {
                        softKeyboard?.hide()
                        uiEvent(
                            CalibrationUiEvent.InsertData(
                                selectedTabIndex,
                                volume.toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                }) {
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
    }
}
