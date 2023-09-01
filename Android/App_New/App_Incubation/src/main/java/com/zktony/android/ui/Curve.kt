package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.zktony.android.data.entities.Curve
import com.zktony.android.ui.components.CurveAppBar
import com.zktony.android.ui.components.CurveItem
import com.zktony.android.ui.components.PointItem
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/30 11:10
 */

@Composable
fun CurveRoute(
    navController: NavHostController,
    viewModel: CurveViewModel,
    snackbarHostState: SnackbarHostState
) {

    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.CURVE_LIST -> navController.navigateUp()
                else -> viewModel.uiEvent(CurveUiEvent.NavTo(PageType.CURVE_LIST))
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

    Scaffold(
        topBar = {
            CurveAppBar(
                entities = entities,
                uiState = uiState,
                uiEvent = viewModel::uiEvent,
                snackbarHostState = snackbarHostState
            ) { navigation() }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        CurveScreen(
            modifier = Modifier.padding(paddingValues),
            entities = entities,
            uiState = uiState,
            uiEvent = viewModel::uiEvent
        )
    }
}

@Composable
fun CurveScreen(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Curve>,
    uiState: CurveUiState,
    uiEvent: (CurveUiEvent) -> Unit,
) {
    AnimatedVisibility(visible = uiState.page == PageType.CURVE_LIST) {
        CurveList(modifier, entities, uiState, uiEvent)
    }

    AnimatedVisibility(visible = uiState.page == PageType.CURVE_DETAIL) {
        CurveDetail(modifier, entities, uiState, uiEvent)
    }
}

@Composable
fun CurveList(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Curve>,
    uiState: CurveUiState,
    uiEvent: (CurveUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            count = entities.itemCount,
            key = entities.itemKey(),
            contentType = entities.itemContentType()
        ) { index ->
            val item = entities[index]
            if (item != null) {
                val color =
                    if (uiState.selected == item.id) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }

                CurveItem(
                    modifier = Modifier.background(
                        color = color,
                        shape = MaterialTheme.shapes.medium
                    ),
                    index = index,
                    curve = item,
                    onClick = {
                        scope.launch {
                            if (uiState.selected == 0L) {
                                uiEvent(CurveUiEvent.ToggleSelected(it.id))
                            } else {
                                uiEvent(CurveUiEvent.ToggleSelected(0L))
                            }
                        }
                    },
                    onDoubleClick = {
                        scope.launch {
                            uiEvent(CurveUiEvent.ToggleSelected(it.id))
                            uiEvent(CurveUiEvent.NavTo(PageType.CURVE_DETAIL))
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CurveDetail(
    modifier: Modifier = Modifier,
    entities: LazyPagingItems<Curve>,
    uiState: CurveUiState,
    uiEvent: (CurveUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    val forceManager = LocalFocusManager.current

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val selected = entities.itemSnapshotList.items.find { it.id == uiState.selected }

        if (selected != null) {
            item {
                Row(
                    modifier = Modifier
                        .height(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "泵编号", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    BasicTextField(
                        modifier = Modifier.width(128.dp),
                        value = TextFieldValue(
                            selected.index.toString(),
                            TextRange(selected.index.toString().length)
                        ),
                        onValueChange = {
                            scope.launch {
                                uiEvent(
                                    CurveUiEvent.Update(
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
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "是否启用", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        modifier = Modifier.height(32.dp),
                        checked = selected.enable,
                        onCheckedChange = {
                            scope.launch {
                                uiEvent(CurveUiEvent.Update(selected.copy(enable = it)))
                            }
                        }
                    )
                }
            }
            itemsIndexed(items = selected.points) { index, item ->
                PointItem(
                    index = index,
                    point = item,
                    onClickOne = { },
                    onClickTwo = {
                        scope.launch {
                            val points = selected.points.toMutableList()
                            points -= item
                            uiEvent(CurveUiEvent.Update(selected.copy(points = points)))
                        }
                    },
                    onPointChange = { point ->
                        scope.launch {
                            val points = selected.points.toMutableList()
                            val index = points.indexOf(item)
                            points[index] = point
                            uiEvent(CurveUiEvent.Update(selected.copy(points = points)))
                        }
                    }
                )
            }
        }
    }
}