package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
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
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.components.Header
import com.zktony.android.ui.components.InputDialog
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.Constants
import com.zktony.android.utils.ext.dateFormat
import com.zktony.android.utils.ext.format
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

    // Handle the back button press
    BackHandler {
        when (uiState.page) {
            PageType.PROGRAM_LIST -> navController.navigateUp() // Step 1: Navigate up if on the list page
            else -> viewModel.event(ProgramEvent.NavTo(PageType.PROGRAM_LIST)) // Step 2: Navigate to the list page if on any other page
        }
    }

    // Display the list page
    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_LIST) {
        ProgramList(
            modifier = modifier,
            uiState = uiState,
            event = viewModel::event,
        )
    }
    // Display the edit page
    AnimatedVisibility(visible = uiState.page == PageType.PROGRAM_DETAIL) {
        ProgramDetail(
            modifier = modifier,
            uiState = uiState,
            event = viewModel::event,
        )
    }
}

/**
 * The ListContent composable function for the app.
 *
 * @param modifier The modifier for the composable.
 * @param uiState The ProgramUiState for the app.
 * @param event The event handler for the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramList(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    event: (ProgramEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    // Show the input dialog if showDialog is true
    if (showDialog) {
        InputDialog(
            onConfirm = {
                scope.launch {
                    val nameList = uiState.entities.map { it.text }
                    if (nameList.contains(it)) {
                        "Name already exists".showShortToast()
                    } else {
                        event(ProgramEvent.Insert(it))
                        showDialog = false
                    }
                }
            },
            onCancel = { showDialog = false },
        )
    }

    // Display the list and operation columns
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        // Display the operation column
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
                                event(ProgramEvent.Delete(uiState.selected))
                                event(ProgramEvent.ToggleSelected(0L))
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
                    onClick = { event(ProgramEvent.NavTo(PageType.PROGRAM_DETAIL)) },
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
                .shadow(
                    elevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
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
                            event(ProgramEvent.ToggleSelected(0L))
                        } else {
                            event(ProgramEvent.ToggleSelected(item.id))
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
    event: (ProgramEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val entity = uiState.entities.find { it.id == uiState.selected } ?: Program()
    val mx = rememberDataSaverState(key = Constants.MAX_X, default = 0f)
    val my = rememberDataSaverState(key = Constants.MAX_Y, default = 0f)
    var values by remember { mutableStateOf((entity.volume + entity.axis).map { it.format(1) }) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Header(
            onBackPressed = {
                event(ProgramEvent.NavTo(PageType.PROGRAM_LIST))
            },
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = Icons.Default.Edit,
                contentDescription = null,
            )
        }

        LazyColumn(
            modifier = modifier
                .shadow(
                    elevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                )
                .windowInsetsPadding(WindowInsets.imeAnimationSource),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                // volume
                OutlinedCard {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.glue_making),
                                style = MaterialTheme.typography.titleMedium,
                            )

                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = TextFieldValue(values[0], TextRange(values[0].length)),
                                onValueChange = {
                                    scope.launch {
                                        values = values.toMutableList().apply { set(0, it.text) }
                                        val volume = entity.volume.toMutableList()
                                        volume[0] = values[0].toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(volume = volume)))
                                    }
                                },
                                label = { Text(text = stringResource(id = R.string.colloid)) },
                                shape = MaterialTheme.shapes.medium,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboard?.hide()
                                    }
                                ),
                            )
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = TextFieldValue(values[1], TextRange(values[1].length)),
                                onValueChange = {
                                    scope.launch {
                                        values = values.toMutableList().apply { set(1, it.text) }
                                        val volume = entity.volume.toMutableList()
                                        volume[1] = values[1].toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(volume = volume)))
                                    }
                                },
                                label = { Text(text = stringResource(id = R.string.coagulant)) },
                                shape = MaterialTheme.shapes.medium,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboard?.hide()
                                    }
                                ),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.pre_drain),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = TextFieldValue(values[2], TextRange(values[2].length)),
                                onValueChange = {
                                    scope.launch {
                                        values = values.toMutableList().apply { set(2, it.text) }
                                        val volume = entity.volume.toMutableList()
                                        volume[2] = values[2].toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(volume = volume)))
                                    }
                                },
                                label = { Text(text = stringResource(id = R.string.colloid)) },
                                shape = MaterialTheme.shapes.medium,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboard?.hide()
                                    }
                                ),
                            )
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = TextFieldValue(values[3], TextRange(values[3].length)),
                                onValueChange = {
                                    scope.launch {
                                        values = values.toMutableList().apply { set(3, it.text) }
                                        val volume = entity.volume.toMutableList()
                                        volume[3] = values[3].toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(volume = volume)))
                                    }
                                },
                                label = { Text(text = stringResource(id = R.string.coagulant)) },
                                shape = MaterialTheme.shapes.medium,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboard?.hide()
                                    }
                                ),
                            )
                        }
                    }
                }
            }
            item {
                OutlinedCard {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "托盘",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = TextFieldValue(values[4], TextRange(values[4].length)),
                                onValueChange = {
                                    scope.launch {
                                        val num = it.text.toFloatOrNull() ?: 0f
                                        val y = if (num > mx.value) {
                                            mx.value.format(1)
                                        } else if (num < 0) {
                                            "0"
                                        } else {
                                            it.text
                                        }
                                        values = values.toMutableList().apply { set(4, y) }
                                        val axis = entity.axis.toMutableList()
                                        axis[0] = values[4].toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(axis = axis)))
                                    }
                                },
                                label = { Text(text = "坐标(0 ~ ${mx.value.format(1)})") },
                                shape = MaterialTheme.shapes.medium,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboard?.hide()
                                    }
                                ),
                            )
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Button(
                                    modifier = Modifier.width(96.dp),
                                    enabled = !uiState.loading,
                                    onClick = {
                                        scope.launch {
                                            keyboard?.hide()
                                            event(
                                                ProgramEvent.MoveTo(
                                                    0,
                                                    values[4].toFloatOrNull() ?: 0f
                                                )
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "针头",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = TextFieldValue(values[5], TextRange(values[5].length)),
                                onValueChange = {
                                    scope.launch {
                                        val num = it.text.toFloatOrNull() ?: 0f
                                        val z = if (num > my.value) {
                                            my.value.format(1)
                                        } else if (num < 0) {
                                            "0"
                                        } else {
                                            it.text
                                        }
                                        values = values.toMutableList().apply { set(5, z) }
                                        val axis = entity.axis.toMutableList()
                                        axis[1] = values[5].toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(axis = axis)))
                                    }
                                },
                                label = { Text(text = "坐标(0 ~ ${my.value.format(1)})") },
                                shape = MaterialTheme.shapes.medium,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboard?.hide()
                                    }
                                ),
                            )
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Button(
                                    modifier = Modifier.width(96.dp),
                                    enabled = !uiState.loading,
                                    onClick = {
                                        scope.launch {
                                            keyboard?.hide()
                                            event(
                                                ProgramEvent.MoveTo(
                                                    1,
                                                    values[5].toFloatOrNull() ?: 0f
                                                )
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null
                                    )
                                }
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
    // Call the ListContent function and pass in a ProgramUiState object as a parameter
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