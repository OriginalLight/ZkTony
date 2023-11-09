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
import com.zktony.android.data.entities.internal.IncubationStage
import com.zktony.android.ui.components.IncubationStageItem
import com.zktony.android.ui.components.ProgramAppBar
import com.zktony.android.ui.components.ProgramItem
import com.zktony.android.ui.components.SquareTextField
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
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
            when (it) {
                PageType.PROGRAM_LIST -> ProgramList(entities, viewModel::dispatch)
                PageType.PROGRAM_DETAIL -> ProgramDetail(
                    entities.toList(),
                    selected,
                    viewModel::dispatch
                )

                else -> {}
            }
        }
    }
}

@Composable
fun ProgramList(
    entities: LazyPagingItems<Program>,
    dispatch: (ProgramIntent) -> Unit
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
) {
    val scope = rememberCoroutineScope()
    val program = entities.find { it.id == selected } ?: Program()
    val selectedIndex = remember { mutableIntStateOf(0) }
    val incubationStage = program.stages.getOrNull(selectedIndex.intValue)

    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(0.5f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(program.stages) { index, item ->
                IncubationStageItem(
                    stage = item,
                    selected = index == selectedIndex.intValue
                ) { func ->
                    scope.launch {
                        when (func) {
                            0 -> {
                                selectedIndex.intValue = index
                            }

                            1, 2 -> {
                                val stages = program.stages.toMutableList()
                                val temp = stages[index]
                                if (func == 1) {
                                    if (index == 0) {
                                        return@launch
                                    }
                                    stages[index] = stages[index - 1]
                                    stages[index - 1] = temp
                                } else {
                                    if (index == stages.size - 1) {
                                        return@launch
                                    }
                                    stages[index] = stages[index + 1]
                                    stages[index + 1] = temp
                                }
                                dispatch(ProgramIntent.Update(program.copy(stages = stages)))
                            }

                            3 -> {
                                val processes = program.stages.toMutableList()
                                processes.removeAt(index)
                                dispatch(ProgramIntent.Update(program.copy(stages = processes)))
                            }
                        }
                    }
                }
            }
        }

        incubationStage?.let {
            ProgramInput(
                modifier = Modifier.fillMaxWidth(),
                key = selectedIndex.intValue,
                incubationStage = incubationStage
            ) { p ->
                scope.launch {
                    val processes = program.stages.toMutableList()
                    processes[selectedIndex.intValue] = p
                    dispatch(ProgramIntent.Update(program.copy(stages = processes)))
                }
            }
        }
    }
}

@Composable
fun ProgramInput(
    modifier: Modifier = Modifier,
    key: Int,
    incubationStage: IncubationStage,
    onProcessChange: (IncubationStage) -> Unit
) {
    val scope = rememberCoroutineScope()
    var typeExpand by remember(key) { mutableStateOf(false) }
    var temperature by remember(key) { mutableStateOf(incubationStage.temperature.toString()) }
    var duration by remember(key) { mutableStateOf(incubationStage.duration.toString()) }
    var dosage by remember(key) { mutableStateOf(incubationStage.dosage.toString()) }
    var origin by remember(key) { mutableIntStateOf(incubationStage.origin) }
    var recycle by remember(key) { mutableStateOf(incubationStage.recycle) }
    var times by remember(key) { mutableStateOf(incubationStage.times.toString()) }

    LazyColumn(
        modifier = modifier.imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        val displayText: @Composable (Int) -> String = {
            when (it) {
                0 -> stringResource(id = R.string.blocking)
                1 -> stringResource(id = R.string.primary_antibody)
                2 -> stringResource(id = R.string.secondary_antibody)
                3 -> stringResource(id = R.string.washing)
                4 -> stringResource(id = R.string.phosphate_buffered_saline)
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
                    text = displayText(incubationStage.type),
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
                                onProcessChange(incubationStage.copy(type = type))
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
                    if (temp != incubationStage.temperature) {
                        onProcessChange(incubationStage.copy(temperature = temp))
                    }
                }
            }
        }

        if (incubationStage.type != 4) {
            item {
                SquareTextField(
                    title = "时长",
                    value = duration,
                    trailingIcon = {
                        Text(
                            modifier = Modifier.padding(end = 16.dp),
                            text = if (incubationStage.type == 3) "Min" else "Hour",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                ) {
                    scope.launch {
                        duration = it
                        val time = it.toDoubleOrNull() ?: 0.0
                        if (time != incubationStage.duration) {
                            onProcessChange(incubationStage.copy(duration = time))
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
                    if (incubationStage.type == 1 || incubationStage.type == 2) {
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
                                        onProcessChange(incubationStage.copy(origin = origin))
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
                    if (volume != incubationStage.dosage) {
                        onProcessChange(incubationStage.copy(dosage = volume))
                    }
                }
            }
        }

        if (incubationStage.type == 1) {
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
                            onProcessChange(incubationStage.copy(recycle = it))
                        }
                    })
                }
            }
        }

        if (incubationStage.type == 3) {
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
                        if (count != incubationStage.times) {
                            onProcessChange(incubationStage.copy(times = count))
                        }
                    }
                }
            }
        }
    }
}