package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.entities.Calibration
import com.zktony.android.ui.components.CalibrationAppBar
import com.zktony.android.ui.components.CalibrationItem
import com.zktony.android.ui.components.PointItem
import com.zktony.android.ui.utils.*
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/30 11:10
 */

@Composable
fun CalibrationRoute(viewModel: CalibrationViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.CALIBRATION_LIST -> navigationActions.navigateUp()
                else -> viewModel.uiEvent(CalibrationUiEvent.NavTo(PageType.CALIBRATION_LIST))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = message) {
        if (message != null) {
            snackbarHostState.showSnackbar(
                message = message ?: "未知错误",
                actionLabel = "关闭",
                duration = SnackbarDuration.Short
            )
        }
    }

    CalibrationScreen(
        entities = entities,
        uiState = uiState,
        uiEvent = viewModel::uiEvent,
        navigation = navigation
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalibrationScreen(
    entities: LazyPagingItems<Calibration>,
    uiState: CalibrationUiState,
    uiEvent: (CalibrationUiEvent) -> Unit,
    navigation: () -> Unit
) {
    Column {
        CalibrationAppBar(entities.toList(), uiState, uiEvent) { navigation() }
        AnimatedContent(targetState = uiState.page) {
            when (uiState.page) {
                PageType.CALIBRATION_LIST -> CalibrationList(entities, uiState, uiEvent)
                PageType.CALIBRATION_DETAIL -> CalibrationDetail(
                    entities.toList(),
                    uiState,
                    uiEvent
                )

                else -> {}
            }
        }
    }
}

@Composable
fun CalibrationList(
    entities: LazyPagingItems<Calibration>,
    uiState: CalibrationUiState,
    uiEvent: (CalibrationUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = Modifier,
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(entities) { index, item ->
            CalibrationItem(
                index = index,
                item = item,
                selected = uiState.selected == item.id
            ) { double ->
                scope.launch {
                    if (double) {
                        uiEvent(CalibrationUiEvent.ToggleSelected(item.id))
                        uiEvent(CalibrationUiEvent.NavTo(PageType.CALIBRATION_DETAIL))
                    } else {
                        if (uiState.selected != item.id) {
                            uiEvent(CalibrationUiEvent.ToggleSelected(item.id))
                        } else {
                            uiEvent(CalibrationUiEvent.ToggleSelected(0L))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CalibrationDetail(
    entities: List<Calibration>,
    uiState: CalibrationUiState,
    uiEvent: (CalibrationUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    val forceManager = LocalFocusManager.current
    val selected = entities.find { it.id == uiState.selected } ?: Calibration(displayText = "None")

    LazyVerticalGrid(
        modifier = Modifier,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "泵编号", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                BasicTextField(
                    modifier = Modifier.width(64.dp),
                    value = TextFieldValue(
                        selected.index.toString(),
                        TextRange(selected.index.toString().length)
                    ),
                    onValueChange = {
                        scope.launch {
                            uiEvent(
                                CalibrationUiEvent.Update(
                                    selected.copy(
                                        index = it.text.toIntOrNull() ?: 0
                                    )
                                )
                            )
                        }
                    },
                    textStyle = TextStyle(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            softKeyboard?.hide()
                            forceManager.clearFocus()
                        }
                    ),
                    decorationBox = @Composable { innerTextField ->
                        Column {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Numbers,
                                    contentDescription = null
                                )
                                innerTextField()
                            }
                            Divider()
                        }
                    }
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "是否生效", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    modifier = Modifier.height(32.dp),
                    checked = selected.enable,
                    onCheckedChange = {
                        scope.launch {
                            uiEvent(CalibrationUiEvent.Update(selected.copy(enable = it)))
                        }
                    }
                )
            }
        }
        itemsIndexed(items = selected.points) { index, item ->
            PointItem(
                index = index,
                item = item,
                onClick = { flag ->
                    scope.launch {
                        if (flag == 0) {
                            //TODO 添加
                        } else {
                            val points = selected.points.toMutableList()
                            points -= item
                            uiEvent(CalibrationUiEvent.Update(selected.copy(points = points)))
                        }
                    }
                },
            ) { point ->
                scope.launch {
                    val points = selected.points.toMutableList()
                    points[points.indexOf(item)] = point
                    uiEvent(CalibrationUiEvent.Update(selected.copy(points = points)))
                }
            }
        }
    }
}