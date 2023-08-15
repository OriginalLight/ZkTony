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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Coordinate
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.CircleTextField
import com.zktony.android.ui.components.CoordinateInput
import com.zktony.android.ui.components.Header
import com.zktony.android.ui.components.InputDialog
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.ext.dateFormat
import com.zktony.android.utils.ext.format
import com.zktony.android.utils.ext.serial
import com.zktony.android.utils.ext.showShortToast
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


/**
 * The Program composable function for the app.
 *
 * @param modifier The modifier for the composable.
 * @param navController The NavHostController for the app.
 * @param viewModel The ProgramViewModel for the app.
 */
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
            event = viewModel::uiEvent,
        )
    }

    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_DETAIL) {
        ProgramDetail(
            modifier = modifier,
            uiState = uiState,
            event = viewModel::uiEvent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramList(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    event: (ProgramUiEvent) -> Unit = {},
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
                        event(ProgramUiEvent.Insert(it))
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
                                event(ProgramUiEvent.Delete(uiState.selected))
                                event(ProgramUiEvent.ToggleSelected(0L))
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
                    onClick = { event(ProgramUiEvent.NavTo(PageType.PROGRAM_DETAIL)) },
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
                            event(ProgramUiEvent.ToggleSelected(0L))
                        } else {
                            event(ProgramUiEvent.ToggleSelected(item.id))
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

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class,
)
@Composable
fun ProgramDetail(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    event: (ProgramUiEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val selected = uiState.entities.find { it.id == uiState.selected } ?: Program()
    val maxAbscissa by rememberDataSaverState(key = Constants.MAX_ABSCISSA, initialValue = 0.0)
    val maxOrdinate by rememberDataSaverState(key = Constants.MAX_ORDINATE, initialValue = 0.0)
    var colloid by remember { mutableStateOf(selected.dosage.colloid.format(1)) }
    var coagulant by remember { mutableStateOf(selected.dosage.coagulant.format(1)) }
    var preColloid by remember { mutableStateOf(selected.dosage.preColloid.format(1)) }
    var preCoagulant by remember { mutableStateOf(selected.dosage.preCoagulant.format(1)) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Header(
            onBackPressed = {
                event(ProgramUiEvent.NavTo(PageType.PROGRAM_LIST))
            },
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
        }

        LazyColumn(
            modifier = modifier
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.medium
                )
                .windowInsetsPadding(WindowInsets.imeAnimationSource),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircleTextField(
                        modifier = Modifier.weight(1f),
                        title = "制胶/促凝剂",
                        value = coagulant,
                        onValueChange = {
                            scope.launch {
                                coagulant = it
                                val dosage =
                                    selected.dosage.copy(coagulant = it.toDoubleOrNull() ?: 0.0)
                                event(ProgramUiEvent.Update(selected.copy(dosage = dosage)))
                            }
                        }
                    )
                    CircleTextField(
                        modifier = Modifier.weight(1f),
                        title = "制胶/胶体",
                        value = colloid,
                        onValueChange = {
                            scope.launch {
                                colloid = it
                                val dosage =
                                    selected.dosage.copy(colloid = it.toDoubleOrNull() ?: 0.0)
                                event(ProgramUiEvent.Update(selected.copy(dosage = dosage)))
                            }
                        }
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircleTextField(
                        modifier = Modifier.weight(1f),
                        title = "预排/促凝剂",
                        value = preCoagulant,
                        onValueChange = {
                            scope.launch {
                                preCoagulant = it
                                val dosage =
                                    selected.dosage.copy(preCoagulant = it.toDoubleOrNull() ?: 0.0)
                                event(ProgramUiEvent.Update(selected.copy(dosage = dosage)))
                            }
                        }
                    )
                    CircleTextField(
                        modifier = Modifier.weight(1f),
                        title = "预排/胶体",
                        value = preColloid,
                        onValueChange = {
                            scope.launch {
                                preColloid = it
                                val dosage =
                                    selected.dosage.copy(preColloid = it.toDoubleOrNull() ?: 0.0)
                                event(ProgramUiEvent.Update(selected.copy(dosage = dosage)))
                            }
                        }
                    )
                }
            }
            item {
                CoordinateInput(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    title = "位置",
                    coordinate = selected.coordinate,
                    limit = Coordinate(maxAbscissa, maxOrdinate),
                    onCoordinateChange = {
                        scope.launch {
                            event(ProgramUiEvent.Update(selected.copy(coordinate = it)))
                        }
                    }
                ) {
                    scope.launch {
                        serial {
                            move {
                                index = 1
                                dv = 0.0
                            }
                            move {
                                index = 0
                                dv = selected.coordinate.abscissa
                            }
                            move {
                                index = 1
                                dv = selected.coordinate.ordinate
                            }
                        }
                    }
                }
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
    // Call the EditContent function and pass in a ProgramUiState object as a parameter
    ProgramDetail(
        uiState = ProgramUiState(
            entities = listOf(
                Program(text = "test", id = 1L)
            ),
            selected = 1L
        )
    )
}