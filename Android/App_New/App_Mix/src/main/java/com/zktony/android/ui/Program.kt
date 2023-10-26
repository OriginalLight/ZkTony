package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.zktony.android.data.datastore.rememberDataSaverState
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.internal.Point
import com.zktony.android.ui.components.CircleTextField
import com.zktony.android.ui.components.CoordinateInput
import com.zktony.android.ui.components.ProgramAppBar
import com.zktony.android.ui.components.ProgramItem
import com.zktony.android.ui.utils.AnimatedContent
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.LocalSnackbarHostState
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.itemsIndexed
import com.zktony.android.ui.utils.toList
import com.zktony.android.utils.Constants
import com.zktony.android.utils.SerialPortUtils.start
import com.zktony.android.utils.extra.format
import kotlinx.coroutines.launch

@Composable
fun ProgramRoute(viewModel: ProgramViewModel) {

    val scope = rememberCoroutineScope()
    val navigationActions = LocalNavigationActions.current
    val snackbarHostState = LocalSnackbarHostState.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val entities = viewModel.entities.collectAsLazyPagingItems()
    val navigation: () -> Unit = {
        scope.launch {
            when (uiState.page) {
                PageType.PROGRAM_LIST -> navigationActions.navigateUp()
                else -> viewModel.uiEvent(ProgramUiEvent.NavTo(PageType.PROGRAM_LIST))
            }
        }
    }

    BackHandler { navigation() }

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
            when (uiState.page) {
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
    uiEvent: (ProgramUiEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val selected = entities.find { it.id == uiState.selected } ?: Program()
    val maxAbscissa by rememberDataSaverState(key = Constants.ZT_0001, initialValue = 0.0)
    val maxOrdinate by rememberDataSaverState(key = Constants.ZT_0002, initialValue = 0.0)
    var colloid by remember { mutableStateOf(selected.dosage.colloid.format(1)) }
    var coagulant by remember { mutableStateOf(selected.dosage.coagulant.format(1)) }
    var preColloid by remember { mutableStateOf(selected.dosage.preColloid.format(1)) }
    var preCoagulant by remember { mutableStateOf(selected.dosage.preCoagulant.format(1)) }
    var glueSpeed by remember { mutableStateOf(selected.speed.glue.format(1)) }
    var preSpeed by remember { mutableStateOf(selected.speed.pre.format(1)) }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CircleTextField(
                    modifier = Modifier.weight(1f),
                    title = "制胶/促凝剂 μL",
                    value = coagulant,
                    onValueChange = {
                        scope.launch {
                            coagulant = it
                            val dosage =
                                selected.dosage.copy(coagulant = it.toDoubleOrNull() ?: 0.0)
                            uiEvent(ProgramUiEvent.Update(selected.copy(dosage = dosage)))
                        }
                    }
                )
                CircleTextField(
                    modifier = Modifier.weight(1f),
                    title = "制胶/胶体 μL",
                    value = colloid,
                    onValueChange = {
                        scope.launch {
                            colloid = it
                            val dosage =
                                selected.dosage.copy(colloid = it.toDoubleOrNull() ?: 0.0)
                            uiEvent(ProgramUiEvent.Update(selected.copy(dosage = dosage)))
                        }
                    }
                )
            }
        }
        item {
            CircleTextField(
                title = "制胶/速度",
                value = glueSpeed,
                onValueChange = {
                    scope.launch {
                        glueSpeed = it
                        val speed =
                            selected.speed.copy(glue = it.toDoubleOrNull() ?: 0.0)
                        uiEvent(ProgramUiEvent.Update(selected.copy(speed = speed)))
                    }
                }
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CircleTextField(
                    modifier = Modifier.weight(1f),
                    title = "预排/促凝剂 μL",
                    value = preCoagulant,
                    onValueChange = {
                        scope.launch {
                            preCoagulant = it
                            val dosage =
                                selected.dosage.copy(preCoagulant = it.toDoubleOrNull() ?: 0.0)
                            uiEvent(ProgramUiEvent.Update(selected.copy(dosage = dosage)))
                        }
                    }
                )
                CircleTextField(
                    modifier = Modifier.weight(1f),
                    title = "预排/胶体 μL",
                    value = preColloid,
                    onValueChange = {
                        scope.launch {
                            preColloid = it
                            val dosage =
                                selected.dosage.copy(preColloid = it.toDoubleOrNull() ?: 0.0)
                            uiEvent(ProgramUiEvent.Update(selected.copy(dosage = dosage)))
                        }
                    }
                )
            }
        }
        item {
            CircleTextField(
                title = "预排/速度",
                value = preSpeed,
                onValueChange = {
                    scope.launch {
                        preSpeed = it
                        val speed =
                            selected.speed.copy(pre = it.toDoubleOrNull() ?: 0.0)
                        uiEvent(ProgramUiEvent.Update(selected.copy(speed = speed)))
                    }
                }
            )
        }
        item {
            CoordinateInput(
                modifier = Modifier.fillMaxWidth(),
                title = "位置",
                point = selected.point,
                limit = Point(maxAbscissa, maxOrdinate),
                onCoordinateChange = {
                    scope.launch {
                        uiEvent(ProgramUiEvent.Update(selected.copy(point = it)))
                    }
                }
            ) {
                scope.launch {
                    start {
                        timeOut = 1000L * 60L
                        with(index = 1, pdv = 0.0)
                    }
                    start {
                        timeOut = 1000L * 60L
                        with(index = 0, pdv = selected.point.x)
                    }
                    start {
                        timeOut = 1000L * 60L
                        with(index = 1, pdv = selected.point.y)
                    }
                }
            }
        }
    }
}