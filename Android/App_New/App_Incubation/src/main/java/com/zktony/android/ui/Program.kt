package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Coordinate
import com.zktony.android.data.entities.Orifice
import com.zktony.android.data.entities.OrificePlate
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.*
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.format
import com.zktony.android.utils.extra.serial
import com.zktony.android.utils.extra.showShortToast
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun Program(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ProgramViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        when (uiState.page) {
            PageType.PROGRAM_LIST -> navController.navigateUp()
            else -> viewModel.uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_LIST))
        }
    }

    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_LIST) {
        ProgramList(
            modifier = modifier,
            uiState = uiState,
            uiEvent = viewModel::uiEvent,
        )
    }

    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_DETAIL) {
        ProgramDetail(
            modifier = modifier,
            uiState = uiState,
            uiEvent = viewModel::uiEvent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramList(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    uiEvent: (ProgramUiEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    if (showDialog) {
        InputDialog(
            onConfirm = {
                scope.launch {
                    val nameList = uiState.entities.map { it.text }
                    if (nameList.contains(it)) {
                        "Name already exists".showShortToast()
                    } else {
                        uiEvent(ProgramUiEvent.Insert(it))
                        showDialog = false
                    }
                }
            },
            onCancel = { showDialog = false },
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { active = false },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("搜索") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val items =
                        uiState.entities.filter { query.isNotEmpty() && it.text.contains(query) }
                    items(items.size) {
                        val item = items[it]
                        ListItem(
                            headlineContent = { Text(item.text) },
                            supportingContent = { Text(item.createTime.dateFormat("yyyy/MM/dd")) },
                            leadingContent = {
                                if (item.text == query) Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.clickable {
                                query = item.text
                                active = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            // Button for adding a new item
            FloatingActionButton(
                modifier = Modifier.sizeIn(minWidth = 64.dp, maxWidth = 128.dp),
                onClick = { showDialog = true })
            {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                )
            }
            // Delete button
            AnimatedVisibility(visible = uiState.selected != 0L) {
                var count by remember { mutableStateOf(0) }

                FloatingActionButton(
                    modifier = Modifier.sizeIn(minWidth = 64.dp, maxWidth = 128.dp),
                    onClick = {
                        scope.launch {
                            if (count == 1) {
                                uiEvent(ProgramUiEvent.Delete(uiState.selected))
                                uiEvent(ProgramUiEvent.ToggleSelected(0L))
                                count = 0
                            } else {
                                count++
                            }
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = if (count == 1) Color.Red else Color.Black,
                    )
                }
            }
            // Edit button
            AnimatedVisibility(visible = uiState.selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier.sizeIn(minWidth = 64.dp, maxWidth = 128.dp),
                    onClick = { uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_DETAIL)) },
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }
        }

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.medium
                ),
            state = gridState,
            contentPadding = PaddingValues(16.dp),
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val items = uiState.entities.filter { it.text.contains(query) }

            itemsIndexed(items = items) { index, item ->
                val background = if (item.id == uiState.selected) {
                    Color.Blue.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
                ElevatedCard(
                    colors = CardDefaults.cardColors(containerColor = background),
                    onClick = {
                        if (item.id == uiState.selected) {
                            uiEvent(ProgramUiEvent.ToggleSelected(0L))
                        } else {
                            uiEvent(ProgramUiEvent.ToggleSelected(item.id))
                        }
                    },
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
}

@Composable
fun ProgramDetail(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    uiEvent: (ProgramUiEvent) -> Unit = {},
) {

    val scope = rememberCoroutineScope()
    val selected = uiState.entities.find { it.id == uiState.selected } ?: Program()
    val orificePlate = remember { mutableStateOf(-1) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 32.dp, vertical = 4.dp)
            ) {
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
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(visible = orificePlate.value == -1) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            val orificePlates = selected.orificePlates.toMutableList()
                            orificePlates.add(OrificePlate())
                            uiEvent(ProgramUiEvent.Update(selected.copy(orificePlates = orificePlates)))
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
            FloatingActionButton(
                onClick = {
                    if (orificePlate.value > -1) {
                        orificePlate.value = -1
                    } else {
                        uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_LIST))
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
            }
        }

        AnimatedVisibility(visible = orificePlate.value == -1) {
            OrificePlateList(
                selected = selected,
                uiEvent = uiEvent,
                toggleSelected = { orificePlate.value = it },
            )
        }

        AnimatedVisibility(visible = orificePlate.value > -1) {
            OrificePlateDetail(
                orificePlate = selected.orificePlates.getOrNull(orificePlate.value)
                    ?: OrificePlate(),
                toggleSelected = {
                    scope.launch {
                        val array = selected.orificePlates.toMutableList()
                        array[orificePlate.value] = it
                        uiEvent(ProgramUiEvent.Update(selected.copy(orificePlates = array)))
                    }
                },
            )
        }
    }
}

@Composable
fun OrificePlateList(
    modifier: Modifier = Modifier,
    selected: Program,
    uiEvent: (ProgramUiEvent) -> Unit = {},
    toggleSelected: (Int) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium
            ),
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
fun OrificePlateDetail(
    modifier: Modifier = Modifier,
    orificePlate: OrificePlate,
    toggleSelected: (OrificePlate) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val abscissa by rememberDataSaverState(key = Constants.ZT_0001, initialValue = 0.0)
    val ordinate by rememberDataSaverState(key = Constants.ZT_0002, initialValue = 0.0)

    var selected by remember { mutableStateOf(orificePlate) }
    var volumeIndex by remember { mutableStateOf(0) }
    var volume by remember { mutableStateOf(selected.getVolume()[0].format(1)) }
    var delay by remember { mutableStateOf(selected.delay.toString()) }

    Row(
        modifier = modifier
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
                                steps = 13,
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
                                steps = 21,
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
                                start(index = 0, dv = selected.coordinate[0].abscissa)
                                start(index = 1, dv = selected.coordinate[0].ordinate)
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
                                start(index = 0, dv = selected.coordinate[1].abscissa)
                                start(index = 1, dv = selected.coordinate[1].ordinate)
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
    val deleteCount = remember { mutableStateOf(0) }

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
                    if (deleteCount.value > 0) {
                        delete()
                        deleteCount.value = 0
                    } else {
                        deleteCount.value++
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = if (deleteCount.value > 0) {
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

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ProgramListPreview() {
    ProgramList(
        uiState = ProgramUiState(
            entities = listOf(
                Program(text = "test")
            )
        )
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ProgramDetailPreview() {
    ProgramDetail(
        uiState = ProgramUiState(
            entities = listOf(Program(text = "test", id = 1L)),
            selected = 1L
        )
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun OrificePlateDetailPreview() {
    OrificePlateDetail(
        orificePlate = OrificePlate(
            row = 12,
            column = 8,
            orifices = listOf(
                listOf(
                    Orifice(),
                    Orifice(),
                    Orifice(),
                ),
                listOf(
                    Orifice(),
                    Orifice(),
                    Orifice(),
                ),
                listOf(
                    Orifice(),
                    Orifice(),
                    Orifice(),
                ),
            )
        )
    )
}