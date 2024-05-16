package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.OrificePlate
import com.zktony.android.data.entities.internal.Point
import com.zktony.android.ui.components.CircleTabRow
import com.zktony.android.ui.components.CircleTextField
import com.zktony.android.ui.components.CoordinateInput
import com.zktony.android.ui.components.OrificePlate
import com.zktony.android.ui.components.OrificePlateCard
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
import com.zktony.android.utils.extra.format
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
                PageType.PROGRAM_EDIT -> viewModel.dispatch(ProgramIntent.NavTo(PageType.PROGRAM_DETAIL))
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

    val selectedPlate = remember { mutableIntStateOf(0) }

    Column {
        ProgramAppBar(entities.toList(), selected, page, viewModel::dispatch) { navigation() }
        AnimatedContent(targetState = page) {
            when (page) {
                PageType.PROGRAM_LIST -> ProgramList(entities, viewModel::dispatch)
                PageType.PROGRAM_DETAIL -> ProgramDetail(
                    entities.toList(),
                    selected,
                    viewModel::dispatch
                ) {
                    scope.launch {
                        selectedPlate.intValue = it
                        viewModel.dispatch(ProgramIntent.NavTo(PageType.PROGRAM_EDIT))
                    }
                }

                PageType.PROGRAM_EDIT -> {
                    val program = entities.itemSnapshotList.items.find { it.id == selected }
                        ?: Program()
                    val orificePlate =
                        program.orificePlates.getOrNull(selectedPlate.intValue) ?: OrificePlate()

                    ProgramInput(orificePlate) {
                        scope.launch {
                            val array = program.orificePlates.toMutableList()
                            array[selectedPlate.intValue] = it
                            viewModel.dispatch(ProgramIntent.Update(program.copy(orificePlates = array)))
                        }
                    }
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
                        dispatch(ProgramIntent.NavTo(PageType.PROGRAM_DETAIL))
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
fun ProgramDetail(
    entities: List<Program>,
    selected: Long,
    dispatch: (ProgramIntent) -> Unit,
    toggleSelected: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val program = entities.find { it.id == selected } ?: Program()

    LazyVerticalGrid(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.small
            ),
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(items = program.orificePlates) { index, item ->
            OrificePlateBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(296.dp),
                orificePlate = item,
                delete = {
                    scope.launch {
                        val array = program.orificePlates.toMutableList()
                        array.removeAt(index)
                        dispatch(ProgramIntent.Update(program.copy(orificePlates = array)))
                        snackbarHostState.showSnackbar("删除成功")
                    }
                },
                toggleSelected = { toggleSelected(index) },
            )
        }
    }
}

@Composable
fun ProgramInput(
    orificePlate: OrificePlate,
    toggleSelected: (OrificePlate) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val abscissa by rememberDataSaverState(key = Constants.ZT_0001, initialValue = 0.0)
    val ordinate by rememberDataSaverState(key = Constants.ZT_0002, initialValue = 0.0)

    var selected by remember { mutableStateOf(orificePlate) }
    var volumeIndex by remember { mutableIntStateOf(0) }
    var volume by remember { mutableStateOf(selected.getVolume()[0].toString()) }
    var previous by remember { mutableStateOf(selected.previous.toString()) }
    var delay by remember { mutableStateOf(selected.delay.toString()) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OrificePlate(
                modifier = Modifier.fillMaxHeight(0.5f),
                row = selected.row,
                column = selected.column,
                selected = selected.getSelected(),
                coordinate = true,
                onItemClick = { size, office ->
                    scope.launch {
                        val rowSpace = size.width / selected.row
                        val columnSpace = size.height / selected.column
                        val list = selected.orifices.toMutableList().map { it.toMutableList() }
                        val x = minOf((office.x / rowSpace).toInt(), selected.row - 1)
                        val y = minOf((office.y / columnSpace).toInt(), selected.column - 1)
                        list[y][x] = list[y][x].copy(selected = !list[y][x].selected)
                        selected = selected.copy(orifices = list)
                        toggleSelected(selected)
                    }
                },
            )

            OrificePlateCard(orificePlate = selected)
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircleTabRow(
                        modifier = Modifier.weight(0.5f),
                        tabItems = listOf("分液模式", "混合模式"),
                        selected = selected.type,
                    ) {
                        scope.launch {
                            selected = selected.copy(type = it)
                            toggleSelected(selected)
                            volumeIndex = 0
                            volume = selected.getVolume()[0].format(1)
                        }
                    }

                    CircleTabRow(
                        modifier = Modifier.weight(0.5f),
                        tabItems = listOf("/", "全选"),
                        selected = if (selected.isSelectAll()) 1 else 0,
                    ) {
                        scope.launch {
                            selected = selected.copy(orifices = selected.selectAll(it == 1))
                            toggleSelected(selected)
                        }
                    }
                }
            }

            item {
                ElevatedCard {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                modifier = Modifier.width(48.dp),
                                text = "竖/${selected.column}",
                                textAlign = TextAlign.Center
                            )
                            Slider(
                                value = selected.column.toFloat(),
                                onValueChange = {
                                    scope.launch {
                                        selected =
                                            selected.copy(
                                                column = it.toInt(),
                                                orifices = emptyList()
                                            )
                                        selected =
                                            selected.copy(orifices = selected.generateOrifices())
                                        toggleSelected(selected)
                                        volumeIndex = 0
                                        volume = "0"
                                    }
                                },
                                valueRange = 2f..16f,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                modifier = Modifier.width(48.dp),
                                text = "横/${selected.row}",
                                textAlign = TextAlign.Center
                            )
                            Slider(
                                value = selected.row.toFloat(),
                                onValueChange = {
                                    scope.launch {
                                        selected =
                                            selected.copy(row = it.toInt(), orifices = emptyList())
                                        selected =
                                            selected.copy(orifices = selected.generateOrifices())
                                        toggleSelected(selected)
                                        volumeIndex = 0
                                        volume = "0"
                                    }
                                },
                                valueRange = 2f..24f,
                            )
                        }
                    }
                }
            }

            item {
                CoordinateInput(
                    title = "A1",
                    limit = Point(abscissa, ordinate),
                    point = selected.points[0],
                    onCoordinateChange = { coordinate ->
                        scope.launch {
                            if (coordinate.x <= abscissa && coordinate.y <= ordinate) {
                                val cd = selected.points.toMutableList()
                                cd[0] = coordinate
                                selected = selected.copy(points = cd)
                                selected = selected.copy(orifices = selected.generateOrifices())
                                toggleSelected(selected)
                            }
                        }
                    }
                ) {
                    scope.launch {
                        start {
                            with(index = 0, pdv = selected.points[0].x)
                            with(index = 1, pdv = selected.points[0].y)
                        }
                    }
                }
            }


            item {
                CoordinateInput(
                    title = "${'A' + selected.column - 1}${selected.row}",
                    limit = Point(abscissa, ordinate),
                    point = selected.points[1],
                    onCoordinateChange = { coordinate ->
                        scope.launch {
                            if (coordinate.x <= abscissa && coordinate.y <= ordinate) {
                                val cd = selected.points.toMutableList()
                                cd[1] = coordinate
                                selected = selected.copy(points = cd)
                                selected = selected.copy(orifices = selected.generateOrifices())
                                toggleSelected(selected)
                            }
                        }
                    }
                ) {
                    scope.launch {
                        start {
                            with(index = 0, pdv = selected.points[1].x)
                            with(index = 1, pdv = selected.points[1].y)
                        }
                    }
                }
            }


            if (selected.type == 1) {
                item {
                    CircleTabRow(
                        modifier = Modifier.weight(0.5f),
                        tabItems = listOf("P1", "P2", "P3", "P4", "P5", "P6"),
                        selected = volumeIndex,
                    ) {
                        scope.launch {
                            volumeIndex = it
                            volume = selected.getVolume()[it].format(1)
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
                        val list = selected.getVolume().toMutableList()
                        list[if (selected.type == 0) 0 else volumeIndex] = v
                        val orifices = selected.setVolume(list)
                        selected = selected.copy(orifices = orifices)
                        toggleSelected(selected)
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
                        selected = selected.copy(previous = it.toDoubleOrNull() ?: 0.0)
                        toggleSelected(selected)
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
                        selected = selected.copy(delay = it.toDoubleOrNull() ?: 0.0)
                        toggleSelected(selected)
                    }
                }
            }
        }
    }
}

@Composable
fun OrificePlateBox(
    modifier: Modifier = Modifier,
    orificePlate: OrificePlate,
    delete: () -> Unit = {},
    toggleSelected: () -> Unit = {},
) {
    val deleteCount = remember { mutableIntStateOf(0) }

    Box(modifier = modifier) {
        OrificePlate(
            modifier = Modifier.fillMaxSize(),
            row = orificePlate.row,
            column = orificePlate.column,
            selected = orificePlate.getSelected(),
        )

        Row(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (8).dp.roundToPx(),
                        y = (-8).dp.roundToPx(),
                    )
                }
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                )
                .align(Alignment.TopEnd),
        ) {
            IconButton(
                modifier = Modifier.size(48.dp),
                onClick = {
                    if (deleteCount.intValue > 0) {
                        delete()
                        deleteCount.intValue = 0
                    } else {
                        deleteCount.intValue++
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = if (deleteCount.intValue > 0) {
                        Color.Red
                    } else {
                        Color.Black
                    },
                )
            }

            IconButton(
                modifier = Modifier.size(48.dp),
                onClick = toggleSelected,
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.Black,
                )
            }
        }
    }
}