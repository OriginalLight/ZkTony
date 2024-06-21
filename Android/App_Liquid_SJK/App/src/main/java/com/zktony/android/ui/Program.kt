package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.OrificePlate
import com.zktony.android.data.entities.internal.Point
import com.zktony.android.ui.components.CircleTextField
import com.zktony.android.ui.components.CoordinateInput
import com.zktony.android.ui.components.OrificePlateWithSelection
import com.zktony.android.ui.components.ProgramAppBar
import com.zktony.android.ui.components.ProgramItem
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.start
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProgramRoute(viewModel: ProgramViewModel) {

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
                PageType.PROGRAM_LIST -> navigationActions.navigateUp()
                else -> viewModel.dispatch(ProgramIntent.NavTo(PageType.PROGRAM_LIST))
            }
        }
    }

    BackHandler { navigation() }

    LaunchedEffect(key1 = uiFlags) {
        if (uiFlags is UiFlags.Message) {
            snackbarHostState.showSnackbar((uiFlags as UiFlags.Message).message)
            viewModel.dispatch(ProgramIntent.Flags(UiFlags.none()))
        }
    }

    Column {
        ProgramAppBar(entities.toList(), selected, page, viewModel::dispatch) { navigation() }
        AnimatedContent(targetState = page) {
            when (page) {
                PageType.PROGRAM_LIST -> ProgramList(entities, viewModel::dispatch)
                PageType.PROGRAM_EDIT -> {
                    ProgramEdit(item = entities.itemSnapshotList.items.find { it.id == selected }
                        ?: Program(), viewModel::dispatch)
                }

                else -> {}
            }
        }
    }
}

@Composable
fun ProgramList(
    entities: LazyPagingItems<Program>,
    dispatch: (ProgramIntent) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    LazyVerticalGrid(
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(entities) { index, item ->
            ProgramItem(
                index = index,
                item = item,
                onClick = {
                    scope.launch {
                        dispatch(ProgramIntent.Selected(item.id))
                        dispatch(ProgramIntent.NavTo(PageType.PROGRAM_EDIT))
                    }
                },
                onDelete = {
                    scope.launch {
                        dispatch(ProgramIntent.Delete(item.id))
                        snackbarHostState.showSnackbar("删除成功")
                    }
                }
            )
        }
    }
}


@Composable
fun ProgramEdit(
    item: Program,
    dispatch: (ProgramIntent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val abscissa by rememberDataSaverState(key = Constants.ZT_0001, initialValue = 0.0)
    val ordinate by rememberDataSaverState(key = Constants.ZT_0002, initialValue = 0.0)
    var orificePlate by remember {
        mutableStateOf(
            item.orificePlates.firstOrNull() ?: OrificePlate()
        )
    }
    var volume by remember { mutableStateOf(orificePlate.getVolume()[0].toString()) }
    var previous by remember { mutableStateOf(orificePlate.previous.toString()) }
    var delay by remember { mutableStateOf(orificePlate.delay.toString()) }

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            OrificePlateWithSelection(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f),
                orificePlate = orificePlate,
                coordinate = true
            ) { intSize, offset ->
                val rowSpace = intSize.width / (orificePlate.row + 1)
                val columnSpace = intSize.height / (orificePlate.column + 1)
                val row = (offset.x / rowSpace).toInt()
                val column = (offset.y / columnSpace).toInt()
                if (row == 0 || column == 0) {
                    var orifices = orificePlate.orifices
                    if (row != 0) {
                        orifices = orifices.filter { o -> o.row == row - 1 }
                    }
                    if (column != 0) {
                        orifices = orifices.filter { o -> o.column == column - 1 }
                    }
                    val checked = orifices.all { o -> o.status > 0 }
                    val lists = orificePlate.orifices.toMutableList()
                    if (checked) {
                        lists.forEachIndexed { index1, o ->
                            if (row == 0 && column == 0) {
                                lists[index1] = o.copy(status = 0)
                            } else if (row == 0) {
                                lists[index1] = if (o.column == column - 1) o.copy(status = 0) else o
                            } else {
                                lists[index1] = if (o.row == row - 1) o.copy(status = 0) else o
                            }
                        }
                    } else {
                        lists.forEachIndexed { index1, o ->
                            if (row == 0 && column == 0) {
                                lists[index1] = o.copy(status = 1)
                            } else if (row == 0) {
                                lists[index1] = if (o.column == column - 1) o.copy(status = 1) else o
                            } else {
                                lists[index1] = if (o.row == row - 1) o.copy(status = 1) else o
                            }
                        }
                    }
                    orificePlate = orificePlate.copy(orifices = lists)
                    dispatch(ProgramIntent.Update(item.copy(orificePlates = listOf(orificePlate))))
                    volume = orificePlate.getVolume()[0].toString()
                    previous = orificePlate.previous.toString()
                    delay = orificePlate.delay.toString()
                } else {
                    val lists = orificePlate.orifices.toMutableList()
                    lists.forEachIndexed { index1, o ->
                        if (o.row == row - 1 && o.column == column - 1) {
                            lists[index1] = if (o.status == 1) o.copy(status = 0) else o.copy(status = 1)
                        }
                    }
                    orificePlate = orificePlate.copy(orifices = lists)
                    dispatch(ProgramIntent.Update(item.copy(orificePlates = listOf(orificePlate))))
                    volume = orificePlate.getVolume()[0].toString()
                    previous = orificePlate.previous.toString()
                    delay = orificePlate.delay.toString()
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.small
                )
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                var row by remember { mutableStateOf(orificePlate.row.toString()) }
                CircleTextField(
                    title = "行数",
                    value = row
                ) {
                    scope.launch {
                        row = it
                        val r = it.toIntOrNull()
                        if (r != null && r > 1) {
                            orificePlate = orificePlate.copy(row = r)
                            orificePlate = orificePlate.copy(orifices = orificePlate.generateOrifices())
                            dispatch(ProgramIntent.Update(item.copy(orificePlates = listOf(orificePlate))))
                            volume = orificePlate.getVolume()[0].toString()
                            previous = orificePlate.previous.toString()
                            delay = orificePlate.delay.toString()
                        }
                    }
                }
            }

            item {
                var column by remember { mutableStateOf(orificePlate.column.toString()) }
                CircleTextField(
                    title = "列数",
                    value = column
                ) {
                    scope.launch {
                        column = it
                        val c = it.toIntOrNull()
                        if (c != null && c > 1) {
                            orificePlate = orificePlate.copy(column = c)
                            orificePlate = orificePlate.copy(orifices = orificePlate.generateOrifices())
                            dispatch(ProgramIntent.Update(item.copy(orificePlates = listOf(orificePlate))))
                            volume = orificePlate.getVolume()[0].toString()
                            previous = orificePlate.previous.toString()
                            delay = orificePlate.delay.toString()
                        }
                    }
                }
            }

            item {
                CoordinateInput(
                    title = "A1",
                    limit = Point(abscissa, ordinate),
                    point = orificePlate.points[0],
                    onCoordinateChange = { coordinate ->
                        scope.launch {
                            if (coordinate.x <= abscissa && coordinate.y <= ordinate) {
                                val cd = orificePlate.points.toMutableList()
                                cd[0] = coordinate
                                orificePlate = orificePlate.copy(points = cd)
                                orificePlate = orificePlate.copy(orifices = orificePlate.generateOrifices())
                                dispatch(ProgramIntent.Update(item.copy(orificePlates = listOf(orificePlate))))
                                volume = orificePlate.getVolume()[0].toString()
                                previous = orificePlate.previous.toString()
                                delay = orificePlate.delay.toString()
                            }
                        }
                    }
                ) {
                    scope.launch {
                        start {
                            with(index = 0, pdv = orificePlate.points[0].x)
                            with(index = 1, pdv = orificePlate.points[0].y)
                        }
                    }
                }
            }


            item {
                CoordinateInput(
                    title = "${'A' + orificePlate.column - 1}${orificePlate.row}",
                    limit = Point(abscissa, ordinate),
                    point = orificePlate.points[1],
                    onCoordinateChange = { coordinate ->
                        scope.launch {
                            if (coordinate.x <= abscissa && coordinate.y <= ordinate) {
                                val cd = orificePlate.points.toMutableList()
                                cd[1] = coordinate
                                orificePlate = orificePlate.copy(points = cd)
                                orificePlate = orificePlate.copy(orifices = orificePlate.generateOrifices())
                                dispatch(ProgramIntent.Update(item.copy(orificePlates = listOf(orificePlate))))
                                volume = orificePlate.getVolume()[0].toString()
                                previous = orificePlate.previous.toString()
                                delay = orificePlate.delay.toString()
                            }
                        }
                    }
                ) {
                    scope.launch {
                        start {
                            with(index = 0, pdv = orificePlate.points[1].x)
                            with(index = 1, pdv = orificePlate.points[1].y)
                        }
                    }
                }
            }

            item {
                CircleTextField(
                    title = "液量 微升",
                    value = volume
                ) {
                    scope.launch {
                        volume = it
                        val v = it.toDoubleOrNull() ?: 0.0
                        val list = orificePlate.getVolume().toMutableList()
                        list[0] = v
                        val orifices = orificePlate.setVolume(list)
                        orificePlate = orificePlate.copy(orifices = orifices)
                        dispatch(ProgramIntent.Update(item.copy(orificePlates = listOf(orificePlate))))
                    }
                }
            }

            item {
                CircleTextField(
                    title = "预排 微升",
                    value = previous
                ) {
                    scope.launch {
                        previous = it
                        orificePlate = orificePlate.copy(previous = it.toDoubleOrNull() ?: 0.0)
                        dispatch(ProgramIntent.Update(item.copy(orificePlates = listOf(orificePlate))))
                    }
                }
            }

            item {
                CircleTextField(
                    title = "延时 秒",
                    value = delay
                ) {
                    scope.launch {
                        delay = it
                        orificePlate = orificePlate.copy(delay = it.toDoubleOrNull() ?: 0.0)
                        dispatch(ProgramIntent.Update(item.copy(orificePlates = listOf(orificePlate))))
                    }
                }
            }
        }
    }
}