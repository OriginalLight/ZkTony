package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/30 11:10
 */

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalibrationRoute(viewModel: CalibrationViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current

    val page by viewModel.page.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val uiFlags by viewModel.uiFlags.collectAsStateWithLifecycle()

    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (page) {
                PageType.CALIBRATION_LIST -> navigationActions.navigateUp()
                else -> viewModel.dispatch(CalibrationIntent.NavTo(PageType.CALIBRATION_LIST))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = uiFlags) {
        if (uiFlags is UiFlags.Message) {
            snackbarHostState.showSnackbar((uiFlags as UiFlags.Message).message)
            viewModel.dispatch(CalibrationIntent.Flags(UiFlags.none()))
        }
    }

    Column {
        CalibrationAppBar(entities.toList(), selected, page, viewModel::dispatch) { navigation() }
        AnimatedContent(targetState = page) {
            when (page) {
                PageType.CALIBRATION_LIST -> CalibrationList(entities, viewModel::dispatch)
                PageType.CALIBRATION_DETAIL -> CalibrationDetail(
                    entities.toList(),
                    selected,
                    uiFlags,
                    viewModel::dispatch
                )

                else -> {}
            }
        }
    }
}

@Composable
fun CalibrationList(
    entities: LazyPagingItems<Calibration>,
    dispatch: (CalibrationIntent) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    LazyVerticalGrid(
        modifier = Modifier,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(entities) { index, item ->
            CalibrationItem(
                index = index,
                item = item,
                onClick = {
                    scope.launch {
                        dispatch(CalibrationIntent.Selected(item.id))
                        dispatch(CalibrationIntent.NavTo(PageType.CALIBRATION_DETAIL))
                    }
                },
                onDelete = {
                    scope.launch {
                        dispatch(CalibrationIntent.Delete(item.id))
                        snackbarHostState.showSnackbar("删除成功")
                    }
                }
            )
        }
    }
}

@Composable
fun CalibrationDetail(
    entities: List<Calibration>,
    selected: Long,
    uiFlags: UiFlags,
    dispatch: (CalibrationIntent) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    val forceManager = LocalFocusManager.current
    val calibration = entities.find { it.id == selected } ?: Calibration(displayText = "None")

    LazyVerticalGrid(
        modifier = Modifier.imePadding(),
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
                Text(text = "编号", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                BasicTextField(
                    modifier = Modifier.width(64.dp),
                    value = TextFieldValue(
                        calibration.index.toString(),
                        TextRange(calibration.index.toString().length)
                    ),
                    onValueChange = {
                        scope.launch {
                            dispatch(
                                CalibrationIntent.Update(
                                    calibration.copy(
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
                            HorizontalDivider()
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
                    checked = calibration.enable,
                    onCheckedChange = {
                        scope.launch {
                            dispatch(CalibrationIntent.Update(calibration.copy(enable = it)))
                        }
                    }
                )
            }
        }
        itemsIndexed(items = calibration.points) { index, item ->
            PointItem(
                key = calibration.points.size,
                index = index,
                item = item,
                uiFlags = uiFlags,
                onClick = { flag ->
                    scope.launch {
                        if (flag == 0) {
                            dispatch(CalibrationIntent.Transfer(calibration.index, item.y))
                        } else {
                            val points = calibration.points.toMutableList()
                            points.remove(item)
                            dispatch(CalibrationIntent.Update(calibration.copy(points = points)))
                        }
                    }
                },
            ) { point ->
                scope.launch {
                    val points = calibration.points.toMutableList()
                    points[points.indexOf(item)] = point
                    dispatch(CalibrationIntent.Update(calibration.copy(points = points)))
                }
            }
        }
    }
}