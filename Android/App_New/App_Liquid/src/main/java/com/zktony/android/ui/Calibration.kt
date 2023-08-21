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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.data.entities.Calibration
import com.zktony.android.ui.components.CircleTabRow
import com.zktony.android.ui.components.InputDialog
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.extra.dateFormat
import com.zktony.android.utils.extra.format
import com.zktony.android.utils.extra.showShortToast
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Calibration screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel CalibrationViewModel
 */
@Composable
fun Calibration(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: CalibrationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        when (uiState.page) {
            PageType.CALIBRATION_LIST -> navController.navigateUp()
            else -> viewModel.uiEvent(CalibrationUiEvent.NavTo(PageType.CALIBRATION_LIST))
        }
    }

    AnimatedVisibility(visible = uiState.page == PageType.CALIBRATION_LIST) {
        CalibrationList(
            modifier = modifier,
            uiState = uiState,
            uiEvent = viewModel::uiEvent,
        )
    }

    AnimatedVisibility(visible = uiState.page == PageType.CALIBRATION_DETAIL) {
        CalibrationDetail(
            modifier = modifier,
            uiState = uiState,
            uiEvent = viewModel::uiEvent,
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationList(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState = CalibrationUiState(),
    uiEvent: (CalibrationUiEvent) -> Unit = {},
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
                        uiEvent(CalibrationUiEvent.Insert(it))
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
        // Column containing the operation buttons
        Row(
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

            // Button for deleting the selected item
            AnimatedVisibility(visible = uiState.selected != 0L) {
                var count by remember { mutableStateOf(0) }

                FloatingActionButton(
                    modifier = Modifier.sizeIn(minWidth = 64.dp, maxWidth = 128.dp),
                    onClick = {
                        if (count == 1) {
                            uiEvent(CalibrationUiEvent.Delete(uiState.selected))
                            uiEvent(CalibrationUiEvent.ToggleSelected(0L))
                            count = 0
                        } else {
                            count++
                        }
                    }) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = if (count == 1) Color.Red else Color.Black,
                    )
                }
            }

            // Button for editing the selected item
            AnimatedVisibility(visible = uiState.selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier.sizeIn(minWidth = 64.dp, maxWidth = 128.dp),
                    onClick = { uiEvent(CalibrationUiEvent.NavTo(PageType.CALIBRATION_DETAIL)) }) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }

            // Button for activating the selected item
            AnimatedVisibility(visible = uiState.selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier.sizeIn(minWidth = 64.dp, maxWidth = 128.dp),
                    onClick = { uiEvent(CalibrationUiEvent.Active(uiState.selected)) }) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Check,
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
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
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
                            uiEvent(CalibrationUiEvent.ToggleSelected(0L))
                        } else {
                            uiEvent(CalibrationUiEvent.ToggleSelected(item.id))
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
                            Spacer(modifier = Modifier.weight(1f))
                            AnimatedVisibility(visible = item.active) {
                                Text(text = "✔")
                            }
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CalibrationDetail(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState = CalibrationUiState(),
    uiEvent: (CalibrationUiEvent) -> Unit = {},
) {

    val entity = uiState.entities.find { it.id == uiState.selected } ?: Calibration()
    val list = remember { mutableStateListOf("M0", "M1", "M2", "M3", "M4", "M5") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var volume by remember { mutableStateOf("") }
    val softKeyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextField(
                modifier = Modifier.weight(1f),
                value = TextFieldValue(volume, TextRange(volume.length)),
                onValueChange = { volume = it.text },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.volume),
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

            AnimatedVisibility(visible = volume.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            softKeyboard?.hide()
                            uiEvent(
                                CalibrationUiEvent.InsertData(
                                    selectedTabIndex,
                                    volume.toDoubleOrNull() ?: 0.0
                                )
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                    )
                }
            }

            // Button for adding a new item
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        softKeyboard?.hide()
                        if (!uiState.loading) {
                            uiEvent(CalibrationUiEvent.AddLiquid(selectedTabIndex))
                        }
                    }
                }
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.Blue,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                    )
                }
            }

            FloatingActionButton(onClick = { uiEvent(CalibrationUiEvent.NavTo(PageType.CALIBRATION_LIST)) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
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
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(items = entity.data) { index, it ->
                Row(
                    modifier = Modifier
                        .background(
                            color = Color.Transparent,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .border(
                            width = 1.dp,
                            color = Color.LightGray,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${index + 1}、",
                        style = MaterialTheme.typography.titleLarge,
                        fontStyle = FontStyle.Italic,
                        color = if (it.first == selectedTabIndex) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Black
                        },
                    )

                    Column {
                        Text(
                            text = list[it.first],
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = it.second.format(2) + " μL",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { uiEvent(CalibrationUiEvent.DeleteData(it)) },
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.Red,
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationListPreview() {
    val entities = listOf(Calibration())
    val uiState = CalibrationUiState(entities = entities)
    CalibrationList(uiState = uiState)
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationDetailPreview() {
    val entities = listOf(Calibration(id = 1L))
    val uiState = CalibrationUiState(entities = entities, selected = 1L)
    CalibrationDetail(uiState = uiState)
}