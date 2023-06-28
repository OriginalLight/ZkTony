package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.logic.data.entities.ProgramEntity
import com.zktony.android.ui.components.CustomTextField
import com.zktony.android.ui.components.DynamicMixPlate
import com.zktony.android.ui.components.InputDialog
import com.zktony.android.ui.components.ZktyTopAppBar
import com.zktony.android.ui.utils.PageType
import com.zktony.core.ext.format
import com.zktony.core.ext.showShortToast
import com.zktony.core.ext.simpleDateFormat
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun ZktyProgram(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ZktyProgramViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        when (uiState.page) {
            PageType.LIST -> navController.navigateUp()
            else -> viewModel.event(ProgramEvent.NavTo(PageType.LIST))
        }
    }

    ContentWrapper(
        modifier = modifier,
        uiState = uiState,
        event = viewModel::event,
    )
}

@Composable
fun ContentWrapper(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState,
    event: (ProgramEvent) -> Unit = {},
) {
    Column(modifier = modifier) {
        // app bar for edit page
        AnimatedVisibility(visible = uiState.page == PageType.EDIT) {
            ZktyTopAppBar(
                title = stringResource(id = R.string.edit),
                navigation = { event(ProgramEvent.NavTo(PageType.LIST)) }
            )
        }
        // list page
        AnimatedVisibility(visible = uiState.page == PageType.LIST) {
            ListContent(
                modifier = Modifier,
                uiState = uiState,
                event = event,
            )
        }
        // edit page
        AnimatedVisibility(visible = uiState.page == PageType.EDIT) {
            EditContent(
                modifier = Modifier,
                uiState = uiState,
                event = event,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListContent(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    event: (ProgramEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val columnState = rememberLazyListState()
    var showDialog by remember { mutableStateOf(false) }

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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // list
        LazyColumn(
            modifier = Modifier
                .weight(6f)
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            state = columnState,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(items = uiState.entities) { index, item ->
                val background = if (item.id == uiState.selected) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
                Card(
                    modifier = Modifier.height(48.dp),
                    colors = CardDefaults.cardColors(containerColor = background),
                    onClick = {
                        if (item.id == uiState.selected) {
                            event(ProgramEvent.ToggleSelected(0L))
                        } else {
                            event(ProgramEvent.ToggleSelected(item.id))
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Image(
                            modifier = Modifier.size(32.dp),
                            painter = painterResource(id = R.drawable.ic_program),
                            contentDescription = null,
                        )
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = item.createTime.simpleDateFormat("yyyy - MM - dd"),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }

        // operation
        Column(
            modifier = modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Add
            FloatingActionButton(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                onClick = { showDialog = true }
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                )
            }
            // Delete
            AnimatedVisibility(visible = uiState.selected != 0L) {
                var count by remember { mutableStateOf(0) }

                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
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
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = if (count == 1) Color.Red else Color.Black,
                    )
                }
            }
            // Edit
            AnimatedVisibility(visible = uiState.selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = { event(ProgramEvent.NavTo(PageType.EDIT)) },
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }
        }
    }
}

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun EditContent(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    event: (ProgramEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val entity = uiState.entities.find { it.id == uiState.selected } ?: ProgramEntity()
    var v1 by remember { mutableStateOf(entity.volume[0].format(1)) }
    var v2 by remember { mutableStateOf(entity.volume[1].format(1)) }
    var v3 by remember { mutableStateOf(entity.volume[2].format(1)) }
    var v4 by remember { mutableStateOf(entity.volume[3].format(1)) }
    var y by remember { mutableStateOf(entity.axis[0].format(1)) }
    var z by remember { mutableStateOf(entity.axis[1].format(1)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium
            ),
    ) {
        Card(
            modifier = Modifier.padding(horizontal = 128.dp, vertical = 16.dp),
        ) {
            DynamicMixPlate(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .padding(horizontal = 16.dp),
                count = 6,
                active = entity.active,
            )
        }
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.imeAnimationSource)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            text = stringResource(id = R.string.actions),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            text = stringResource(id = R.string.colloid),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            text = stringResource(id = R.string.coagulant),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(modifier = Modifier.weight(0.5f)) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            text = stringResource(id = R.string.glue_making),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }

                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = TextFieldValue(v1, TextRange(v1.length)),
                                onValueChange = {
                                    scope.launch {
                                        v1 = it.text
                                        val volume = entity.volume.toMutableList()
                                        volume[0] = v1.toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(volume = volume)))
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                ),
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
                            Icon(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .align(Alignment.CenterEnd),
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    }

                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = TextFieldValue(v2, TextRange(v2.length)),
                                onValueChange = {
                                    scope.launch {
                                        v2 = it.text
                                        val volume = entity.volume.toMutableList()
                                        volume[1] = v2.toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(volume = volume)))
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                ),
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
                            Icon(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .align(Alignment.CenterEnd),
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(modifier = Modifier.weight(0.5f)) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            text = stringResource(id = R.string.pre_drain),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = TextFieldValue(v3, TextRange(v3.length)),
                                onValueChange = {
                                    scope.launch {
                                        v3 = it.text
                                        val volume = entity.volume.toMutableList()
                                        volume[2] = v3.toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(volume = volume)))
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                ),
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
                            Icon(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .align(Alignment.CenterEnd),
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    }
                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = TextFieldValue(v4, TextRange(v4.length)),
                                onValueChange = {
                                    scope.launch {
                                        v4 = it.text
                                        val volume = entity.volume.toMutableList()
                                        volume[3] = v4.toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(volume = volume)))
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                ),
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
                            Icon(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .align(Alignment.CenterEnd),
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(modifier = Modifier.weight(0.5f)) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            text = stringResource(id = R.string.motor),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            text = stringResource(id = R.string.moving_distance),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            text = stringResource(id = R.string.actions),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(modifier = Modifier.weight(0.5f)) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            text = stringResource(id = R.string.y_axis),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = TextFieldValue(y, TextRange(y.length)),
                                onValueChange = {
                                    scope.launch {
                                        y = it.text
                                        val axis = entity.axis.toMutableList()
                                        axis[0] = y.toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(axis = axis)))
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                ),
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
                            Icon(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .align(Alignment.CenterEnd),
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    }
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
                                    event(ProgramEvent.MoveTo(0, y.toFloatOrNull() ?: 0f))
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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(modifier = Modifier.weight(0.5f)) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            text = stringResource(id = R.string.z_axis),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = TextFieldValue(z, TextRange(z.length)),
                                onValueChange = {
                                    scope.launch {
                                        z = it.text
                                        val axis = entity.axis.toMutableList()
                                        axis[1] = z.toFloatOrNull() ?: 0f
                                        event(ProgramEvent.Update(entity.copy(axis = axis)))
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                ),
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
                            Icon(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .align(Alignment.CenterEnd),
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    }
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
                                    event(ProgramEvent.MoveTo(1, z.toFloatOrNull() ?: 0f))
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

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ProgramListContentPreview() {
    ListContent(
        uiState = ProgramUiState(
            entities = listOf(
                ProgramEntity(text = "test")
            )
        )
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ProgramEditContentPreview() {
    EditContent(
        uiState = ProgramUiState(
            entities = listOf(
                ProgramEntity(text = "test", id = 1L)
            ),
            selected = 1L
        )
    )
}

