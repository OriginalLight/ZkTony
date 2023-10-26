package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.R
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.Process
import com.zktony.android.ui.components.ProcessItem
import com.zktony.android.ui.components.ProgramAppBar
import com.zktony.android.ui.components.ProgramItem
import com.zktony.android.ui.components.SquareTextField
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import kotlinx.coroutines.launch

@Composable
fun ProgramRoute(viewModel: ProgramViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.PROGRAM_LIST -> navigationActions.navigateUp()
                else -> viewModel.uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_LIST))
            }
        }
    }


    BackHandler { navigation() }

    LaunchedEffect(key1 = message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.uiEvent(ProgramUiEvent.Message(null))
        }
    }

    ProgramWrapper(
        entities = entities,
        uiState = uiState,
        uiEvent = viewModel::uiEvent,
        navigation = navigation
    )
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProgramWrapper(
    entities: LazyPagingItems<Program>,
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit,
    navigation: () -> Unit
) {
    Column {
        ProgramAppBar(entities.toList(), uiState, uiEvent) { navigation() }
        AnimatedContent(targetState = uiState.page) {
            when (it) {
                PageType.PROGRAM_LIST -> ProgramList(entities, uiEvent)
                PageType.PROGRAM_DETAIL -> ProgramDetail(entities.toList(), uiState, uiEvent)
                else -> {}
            }
        }
    }
}

@Composable
fun ProgramList(
    entities: LazyPagingItems<Program>,
    uiEvent: (ProgramUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    LazyVerticalGrid(
        modifier = Modifier,
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
                        uiEvent(ProgramUiEvent.ToggleSelected(item.id))
                        uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_DETAIL))
                    }
                },
                onDelete = {
                    scope.launch {
                        uiEvent(ProgramUiEvent.Delete(item.id))
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
    uiState: ProgramUiState,
    uiEvent: (ProgramUiEvent) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val selected = entities.find { it.id == uiState.selected } ?: Program()
    val selectedIndex = remember { mutableIntStateOf(0) }
    val process = selected.processes.getOrNull(selectedIndex.intValue)

    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(0.5f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(selected.processes) { index, item ->
                ProcessItem(
                    item = item,
                    selected = index == selectedIndex.intValue
                ) { func ->
                    scope.launch {
                        when (func) {
                            0 -> {
                                selectedIndex.intValue = index
                            }

                            1, 2 -> {
                                val processes = selected.processes.toMutableList()
                                val temp = processes[index]
                                if (func == 1) {
                                    if (index == 0) {
                                        return@launch
                                    }
                                    processes[index] = processes[index - 1]
                                    processes[index - 1] = temp
                                } else {
                                    if (index == processes.size - 1) {
                                        return@launch
                                    }
                                    processes[index] = processes[index + 1]
                                    processes[index + 1] = temp
                                }
                                uiEvent(ProgramUiEvent.Update(selected.copy(processes = processes)))
                            }

                            3 -> {
                                val processes = selected.processes.toMutableList()
                                processes.removeAt(index)
                                uiEvent(ProgramUiEvent.Update(selected.copy(processes = processes)))
                            }
                        }
                    }
                }
            }
        }

        process?.let {
            ProgramInput(
                modifier = Modifier.fillMaxWidth(),
                key = selectedIndex.intValue,
                process = process
            ) { p ->
                scope.launch {
                    val processes = selected.processes.toMutableList()
                    processes[selectedIndex.intValue] = p
                    uiEvent(ProgramUiEvent.Update(selected.copy(processes = processes)))
                }
            }
        }
    }
}

@Composable
fun ProgramInput(
    modifier: Modifier = Modifier,
    key: Int,
    process: Process,
    onProcessChange: (Process) -> Unit
) {
    val scope = rememberCoroutineScope()
    var typeExpand by remember(key) { mutableStateOf(false) }
    var temperature by remember(key) { mutableStateOf(process.temperature.toString()) }
    var duration by remember(key) { mutableStateOf(process.duration.toString()) }
    var dosage by remember(key) { mutableStateOf(process.dosage.toString()) }
    var origin by remember(key) { mutableIntStateOf(process.origin) }
    var recycle by remember(key) { mutableStateOf(process.recycle) }
    var times by remember(key) { mutableStateOf(process.times.toString()) }

    LazyColumn(
        modifier = modifier.imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        val displayText: @Composable (Int) -> String = {
            when (it) {
                Process.BLOCKING -> stringResource(id = R.string.blocking)
                Process.PRIMARY_ANTIBODY -> stringResource(id = R.string.primary_antibody)
                Process.SECONDARY_ANTIBODY -> stringResource(id = R.string.secondary_antibody)
                Process.WASHING -> stringResource(id = R.string.washing)
                Process.PHOSPHATE_BUFFERED_SALINE -> stringResource(id = R.string.phosphate_buffered_saline)
                else -> ""
            }
        }

        item {
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        typeExpand = !typeExpand
                    }
                    .padding(16.dp)
            ) {
                Text(
                    text = displayText(process.type),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (typeExpand) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        }

        if (typeExpand) {
            items(count = 5) { type ->
                Row(
                    modifier = Modifier
                        .padding(start = 32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            scope.launch {
                                typeExpand = false
                                onProcessChange(process.copy(type = type))
                            }
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        text = displayText(type),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null)
                }
            }
        }

        item {
            SquareTextField(
                title = "温度",
                value = temperature,
                trailingIcon = {
                    Text(
                        modifier = Modifier.padding(end = 16.dp),
                        text = "℃",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            ) {
                scope.launch {
                    temperature = it
                    val temp = it.toDoubleOrNull() ?: 0.0
                    if (temp != process.temperature) {
                        onProcessChange(process.copy(temperature = temp))
                    }
                }
            }
        }

        if (process.type != Process.PHOSPHATE_BUFFERED_SALINE) {
            item {
                SquareTextField(
                    title = "时长",
                    value = duration,
                    trailingIcon = {
                        Text(
                            modifier = Modifier.padding(end = 16.dp),
                            text = if (process.type == Process.WASHING) "Min" else "Hour",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                ) {
                    scope.launch {
                        duration = it
                        val time = it.toDoubleOrNull() ?: 0.0
                        if (time != process.duration) {
                            onProcessChange(process.copy(duration = time))
                        }
                    }
                }
            }
        }

        item {
            SquareTextField(
                title = "液量",
                value = dosage,
                trailingIcon = {
                    if (process.type == Process.PRIMARY_ANTIBODY || process.type == Process.SECONDARY_ANTIBODY) {
                        Text(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.small
                                )
                                .clip(MaterialTheme.shapes.small)
                                .clickable {
                                    scope.launch {
                                        origin = (origin + 1) % 5
                                        onProcessChange(process.copy(origin = origin))
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 16.dp),
                            text = "${'@' + origin}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            modifier = Modifier.padding(end = 16.dp),
                            text = "μL",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            ) {
                scope.launch {
                    dosage = it
                    val volume = it.toDoubleOrNull() ?: 0.0
                    if (volume != process.dosage) {
                        onProcessChange(process.copy(dosage = volume))
                    }
                }
            }
        }

        if (process.type == Process.PRIMARY_ANTIBODY) {
            item {
                Row(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        )
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            typeExpand = !typeExpand
                        }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "回收",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = recycle, onCheckedChange = {
                        scope.launch {
                            recycle = it
                            onProcessChange(process.copy(recycle = it))
                        }
                    })
                }
            }
        }

        if (process.type == Process.WASHING) {
            item {
                SquareTextField(
                    title = "次数",
                    value = times,
                    trailingIcon = {
                        Text(
                            modifier = Modifier.padding(end = 16.dp),
                            text = "Cycle",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                ) {
                    scope.launch {
                        times = it
                        val count = it.toIntOrNull() ?: 0
                        if (count != process.times) {
                            onProcessChange(process.copy(times = count))
                        }
                    }
                }
            }
        }
    }
}