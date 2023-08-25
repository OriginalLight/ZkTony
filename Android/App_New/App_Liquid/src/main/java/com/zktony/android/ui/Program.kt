package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Coordinate
import com.zktony.android.data.entities.OrificePlate
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.*
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.format
import com.zktony.android.utils.extra.serial
import kotlinx.coroutines.launch

@Composable
fun ProgramRoute(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ProgramViewModel,
    snackbarHostState: SnackbarHostState,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.PROGRAM_LIST -> navController.navigateUp()
                PageType.PROGRAM_EDIT -> viewModel.uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_DETAIL))
                else -> viewModel.uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_LIST))
            }
        }
    }

    BackHandler { navigation() }

    Scaffold(
        topBar = {
            ProgramAppBar(
                uiState = uiState,
                uiEvent = viewModel::uiEvent,
                navigation = navigation,
                snackbarHostState = snackbarHostState
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        ProgramScreen(
            modifier = modifier.padding(paddingValues),
            uiState = uiState,
            uiEvent = viewModel::uiEvent
        )
    }
}

@Composable
fun ProgramScreen(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit
) {

    val scope = rememberCoroutineScope()
    val selected = remember { mutableIntStateOf(-1) }

    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_LIST) {
        ProgramList(modifier, uiState, uiEvent)
    }

    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_DETAIL) {
        ProgramDetail(modifier, uiState, uiEvent) {
            scope.launch {
                selected.intValue = it
                uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_EDIT))
            }
        }
    }

    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_EDIT) {
        val program = uiState.entities.find { it.id == uiState.selected } ?: Program()
        val orificePlate = program.orificePlates.getOrNull(selected.intValue) ?: OrificePlate()

        ProgramEdit(modifier, orificePlate) {
            scope.launch {
                val array = program.orificePlates.toMutableList()
                array[selected.intValue] = it
                uiEvent(ProgramUiEvent.Update(program.copy(orificePlates = array)))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgramList(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    uiEvent: (ProgramUiEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.small
            ),
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(items = uiState.entities) { index, item ->
            val background = if (item.id == uiState.selected) {
                Color.Blue.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            ElevatedCard(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .combinedClickable(
                        onClick = {
                            scope.launch {
                                if (item.id == uiState.selected) {
                                    uiEvent(ProgramUiEvent.ToggleSelected(0L))
                                } else {
                                    uiEvent(ProgramUiEvent.ToggleSelected(item.id))
                                }
                            }
                        },
                        onDoubleClick = {
                            scope.launch {
                                uiEvent(ProgramUiEvent.ToggleSelected(item.id))
                                uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_DETAIL))
                            }
                        }
                    ),
                colors = CardDefaults.cardColors(containerColor = background)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Display the entity image and title
                    Row(
                        modifier = Modifier.height(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "${index + 1}、",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = item.text,
                        fontSize = 20.sp,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = item.createTime.dateFormat("yyyy/MM/dd"),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

@Composable
fun ProgramDetail(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit,
    toggleSelected: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = modifier
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
        val selected = uiState.entities.find { it.id == uiState.selected } ?: Program()

        itemsIndexed(items = selected.orificePlates) { index, item ->
            OrificePlateBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(296.dp),
                orificePlate = item,
                delete = {
                    scope.launch {
                        val array = selected.orificePlates.toMutableList()
                        array.removeAt(index)
                        uiEvent(ProgramUiEvent.Update(selected.copy(orificePlates = array)))
                    }
                },
                toggleSelected = { toggleSelected(index) },
            )
        }
    }
}

@Composable
fun ProgramEdit(
    modifier: Modifier = Modifier,
    orificePlate: OrificePlate,
    toggleSelected: (OrificePlate) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val abscissa by rememberDataSaverState(key = Constants.ZT_0001, initialValue = 0.0)
    val ordinate by rememberDataSaverState(key = Constants.ZT_0002, initialValue = 0.0)

    var selected by remember { mutableStateOf(orificePlate) }
    var volumeIndex by remember { mutableIntStateOf(0) }
    var volume by remember { mutableStateOf(selected.getVolume()[0].format(1)) }
    var delay by remember { mutableStateOf(selected.delay.toString()) }

    Row(
        modifier = modifier
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
                                text = "C/${selected.column}",
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
                                text = "R/${selected.row}",
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
                    limit = Coordinate(abscissa, ordinate),
                    coordinate = selected.coordinate[0],
                    onCoordinateChange = { coordinate ->
                        scope.launch {
                            if (coordinate.abscissa <= abscissa && coordinate.ordinate <= ordinate) {
                                val cd = selected.coordinate.toMutableList()
                                cd[0] = coordinate
                                selected = selected.copy(coordinate = cd)
                                selected = selected.copy(orifices = selected.generateOrifices())
                                toggleSelected(selected)
                            }
                        }
                    },
                    onClick = {
                        scope.launch {
                            serial {
                                start(index = 0, pdv = selected.coordinate[0].abscissa)
                                start(index = 1, pdv = selected.coordinate[0].ordinate)
                            }
                        }
                    }
                )
            }


            item {
                CoordinateInput(
                    title = "${'A' + selected.column - 1}${selected.row}",
                    limit = Coordinate(abscissa, ordinate),
                    coordinate = selected.coordinate[1],
                    onCoordinateChange = { coordinate ->
                        scope.launch {
                            if (coordinate.abscissa <= abscissa && coordinate.ordinate <= ordinate) {
                                val cd = selected.coordinate.toMutableList()
                                cd[1] = coordinate
                                selected = selected.copy(coordinate = cd)
                                selected = selected.copy(orifices = selected.generateOrifices())
                                toggleSelected(selected)
                            }
                        }
                    },
                    onClick = {
                        scope.launch {
                            serial {
                                start(index = 0, pdv = selected.coordinate[1].abscissa)
                                start(index = 1, pdv = selected.coordinate[1].ordinate)
                            }
                        }
                    }
                )
            }


            if (selected.type == 1) {
                item {
                    CircleTabRow(
                        modifier = Modifier.weight(0.5f),
                        tabItems = listOf("M0", "M1", "M2", "M3", "M4", "M5"),
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
                    title = "液量 μL",
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
                    title = "延时 ms",
                    value = delay
                ) {
                    scope.launch {
                        delay = it
                        selected = selected.copy(delay = it.toLongOrNull() ?: 0L)
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

    Box(
        modifier = modifier
    ) {
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