package com.zktony.android.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.R
import com.zktony.android.core.ext.volume
import com.zktony.android.data.entity.ContainerEntity
import com.zktony.android.data.entity.Point
import com.zktony.android.data.entity.ProgramEntity
import com.zktony.android.ui.components.DynamicMixPlate
import com.zktony.android.ui.components.ZkTonyBottomAddAppBar
import com.zktony.android.ui.components.ZkTonyScaffold
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.utils.PageEnum
import com.zktony.core.ext.format
import com.zktony.core.ext.simpleDateFormat
import kotlinx.coroutines.launch


@Composable
fun ProgramScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ProgramViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    BackHandler {
        if (uiState.page == PageEnum.MAIN) {
            navController.navigateUp()
        } else {
            viewModel.navigationTo(PageEnum.MAIN)
        }
    }

    ZkTonyScaffold(
        modifier = modifier,
        topBar = {
            AnimatedVisibility(visible = uiState.page == PageEnum.EDIT) {
                ZkTonyTopAppBar(
                    title = stringResource(id = R.string.edit),
                    navigation = {
                        if (uiState.page == PageEnum.MAIN) {
                            navController.navigateUp()
                        } else {
                            viewModel.navigationTo(PageEnum.MAIN)
                        }
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = uiState.page == PageEnum.ADD) {
                ZkTonyBottomAddAppBar(
                    strings = uiState.entities.map { it.name },
                    insert = viewModel::insert,
                    navigationTo = viewModel::navigationTo,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        AnimatedVisibility(visible = uiState.page in listOf(PageEnum.MAIN, PageEnum.ADD)) {
            ProgramMainPage(
                modifier = Modifier,
                uiState = uiState,
                delete = viewModel::delete,
                navigationTo = viewModel::navigationTo,
                toggleSelected = viewModel::toggleSelected,
            )
        }
        AnimatedVisibility(visible = uiState.page == PageEnum.EDIT) {
            ProgramEditPage(
                modifier = Modifier,
                entity = uiState.entities.find { it.id == uiState.selected }!!,
                containers = uiState.containers,
                update = viewModel::update,
                toggleActive = viewModel::toggleActive,
                toggleContainer = viewModel::toggleContainer,
                showSnackbar = {
                    scope.launch {
                        snackbarHostState.showSnackbar(it)
                    }
                },
            )
        }
    }
}

@Composable
fun ProgramMainPage(
    modifier: Modifier = Modifier,
    uiState: ProgramUiState = ProgramUiState(),
    delete: (Long) -> Unit = {},
    navigationTo: (PageEnum) -> Unit = {},
    toggleSelected: (Long) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val columnState = rememberLazyListState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        // list
        LazyColumn(
            modifier = Modifier
                .weight(6f)
                .fillMaxHeight()
                .padding(end = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            state = columnState,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.entities.forEach {
                item {
                    val background = if (it.id == uiState.selected) {
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }

                    OutlinedCard(
                        modifier = Modifier
                            .wrapContentHeight()
                            .clickable {
                                if (it.id == uiState.selected) {
                                    toggleSelected(0L)
                                } else {
                                    toggleSelected(it.id)
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = background),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(start = 16.dp),
                                painter = painterResource(id = R.drawable.ic_program),
                                contentDescription = null,
                            )
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = it.name,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = it.createTime.simpleDateFormat("yyyy - MM - dd"),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
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
            FloatingActionButton(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                onClick = { navigationTo(PageEnum.ADD) }
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add),
                )
            }
            AnimatedVisibility(visible = uiState.selected != 0L) {
                var count by remember { mutableStateOf(0) }

                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            if (count == 1) {
                                delete(uiState.selected)
                                toggleSelected(0L)
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
            AnimatedVisibility(visible = uiState.selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = { navigationTo(PageEnum.EDIT) }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@OptIn(
    ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun ProgramEditPage(
    modifier: Modifier = Modifier,
    entity: ProgramEntity = ProgramEntity(),
    containers: List<ContainerEntity> = emptyList(),
    update: (ProgramEntity) -> Unit = {},
    toggleActive: (Int) -> Unit = {},
    toggleContainer: (ContainerEntity) -> Unit = {},
    showSnackbar: (String) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    var v1 by remember { mutableStateOf(entity.volume()[0].format(1)) }
    var v2 by remember { mutableStateOf(entity.volume()[1].format(1)) }
    var v3 by remember { mutableStateOf(entity.volume()[2].format(1)) }
    var v4 by remember { mutableStateOf(entity.volume()[3].format(1)) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.imeAnimationSource)
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Card(
                modifier = Modifier.padding(horizontal = 128.dp, vertical = 16.dp),
            ) {
                DynamicMixPlate(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .padding(horizontal = 16.dp),
                    count = entity.data.size,
                    data = entity.data.map { p -> Pair(p.index, p.active) },
                    onItemClick = { width, x ->
                        scope.launch {
                            val space = width / entity.data.size
                            val index = (x / space).toInt()
                            toggleActive(index)
                        }
                    }
                )
            }
        }
        item {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                containers.forEach {
                    FilterChip(
                        selected = it.id == entity.subId,
                        onClick = {
                            scope.launch {
                                toggleContainer(it)
                                v1 = "0"
                                v2 = "0"
                                v3 = "0"
                                v4 = "0"
                                showSnackbar("已切换容器- ${it.name}")
                            }
                        },
                        label = {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = it.name,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.ic_container),
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        },
                        trailingIcon = if (it.id == entity.subId) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = v1,
                        onValueChange = { v1 = it },
                        shape = MaterialTheme.shapes.medium,
                        label = { Text("制胶-胶体") },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                        ),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        trailingIcon = {
                            Icon(
                                modifier = Modifier
                                    .clickable { v1 = "" },
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = null,
                            )
                        },
                        singleLine = true,
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
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = v2,
                        onValueChange = { v2 = it },
                        shape = MaterialTheme.shapes.medium,
                        label = { Text("制胶-促凝剂") },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                        ),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        trailingIcon = {
                            Icon(
                                modifier = Modifier
                                    .clickable { v2 = "" },
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = null,
                            )
                        },
                        singleLine = true,
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = v3,
                        onValueChange = { v3 = it },
                        shape = MaterialTheme.shapes.medium,
                        label = { Text("预排-胶体") },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                        ),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        trailingIcon = {
                            Icon(
                                modifier = Modifier
                                    .clickable { v3 = "" },
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = null,
                            )
                        },
                        singleLine = true,
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
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = v4,
                        onValueChange = { v4 = it },
                        shape = MaterialTheme.shapes.medium,
                        label = { Text("预排-促凝剂") },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                        ),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        trailingIcon = {
                            Icon(
                                modifier = Modifier
                                    .clickable { v4 = "" },
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = null,
                            )
                        },
                        singleLine = true,
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
                AnimatedVisibility(
                    visible = v1 != entity.volume()[0].toString()
                            || v2 != entity.volume()[1].toString()
                            || v3 != entity.volume()[2].toString()
                            || v4 != entity.volume()[3].toString(),
                ) {
                    FloatingActionButton(
                        modifier = Modifier.width(128.dp),
                        onClick = {
                            scope.launch {
                                update(
                                    entity.copy(
                                        data = entity.data.map { p ->
                                            p.copy(
                                                volume = listOf(
                                                    v1.toFloatOrNull() ?: 0f,
                                                    v2.toFloatOrNull() ?: 0f,
                                                    v3.toFloatOrNull() ?: 0f,
                                                    v4.toFloatOrNull() ?: 0f,
                                                )
                                            )
                                        },
                                    )
                                )
                                showSnackbar("已更新数据")
                            }
                        },
                    ) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ProgramMainPagePreview() {
    ProgramMainPage(
        uiState = ProgramUiState(
            entities = listOf(
                ProgramEntity(name = "test")
            )
        )
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ProgramEditPagePreview() {
    val pointList = mutableListOf<Point>()
    repeat(6) {
        pointList.add(Point())
    }
    val containers = mutableListOf<ContainerEntity>()
    repeat(10) {
        containers.add(ContainerEntity())
    }
    ProgramEditPage(
        entity = ProgramEntity(data = pointList),
        containers = containers,
    )
}

