package com.zktony.android.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
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
import com.zktony.android.data.entity.ContainerEntity
import com.zktony.android.data.entity.Point
import com.zktony.android.ui.components.DynamicMixPlate
import com.zktony.android.ui.components.ZkTonyBottomAddAppBar
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.navigation.PageEnum
import com.zktony.core.ext.Ext
import com.zktony.core.ext.format
import com.zktony.core.ext.simpleDateFormat
import kotlinx.coroutines.launch

/**
 * Container screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel ContainerViewModel
 * @return Unit
 */
@Composable
fun ContainerScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ContainerViewModel,
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

    Scaffold(
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
                    insert = {
                        viewModel.insert(it)
                        viewModel.navigationTo(PageEnum.MAIN)
                    },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedVisibility(
                        visible = uiState.page == PageEnum.MAIN || uiState.page == PageEnum.ADD,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally(),
                    ) {
                        ContainerMainPage(
                            modifier = modifier,
                            uiState = uiState,
                            delete = viewModel::delete,
                            navigationTo = viewModel::navigationTo,
                            toggleSelected = viewModel::toggleSelected,
                        )
                    }
                    AnimatedVisibility(
                        visible = uiState.page == PageEnum.EDIT,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally(),
                    ) {
                        ContainerEditPage(
                            modifier = modifier,
                            entity = uiState.entities.find { it.id == uiState.selected }!!,
                            update = viewModel::update,
                            showSnackbar = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            },
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ContainerMainPage(
    modifier: Modifier = Modifier,
    uiState: ContainerUiState = ContainerUiState(),
    delete: (Long) -> Unit = {},
    navigationTo: (PageEnum) -> Unit = {},
    toggleSelected: (Long) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        val columnState = rememberLazyListState()

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
                                painter = painterResource(id = R.drawable.ic_container),
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
                        if (count == 1) {
                            delete(uiState.selected)
                            toggleSelected(0L)
                            count = 0
                        } else {
                            count++
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.delete),
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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun ContainerEditPage(
    modifier: Modifier = Modifier,
    entity: ContainerEntity = ContainerEntity(),
    update: (ContainerEntity) -> Unit = {},
    showSnackbar: (String) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val softKeyboard = LocalSoftwareKeyboardController.current
    var y by remember { mutableStateOf(entity.data[0].axis[1].format(2)) }
    var z by remember { mutableStateOf(entity.data[0].axis[2].format(2)) }

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
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                TextField(
                    modifier = Modifier
                        .weight(2f)
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.large,
                    value = y,
                    onValueChange = { y = it },
                    label = { Text(text = "容器位置") },
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
                                .clickable { y = "" },
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = null,
                        )
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
                    singleLine = true,
                )
                FloatingActionButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    onClick = { softKeyboard?.hide() },
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Filled.MoveUp,
                        contentDescription = null,
                    )
                }
                FloatingActionButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    onClick = {
                        softKeyboard?.hide()
                        scope.launch {
                            val list = entity.data.toMutableList()
                            update(
                                entity.copy(
                                    data = list.map { point ->
                                        point.copy(
                                            axis = listOf(
                                                0f,
                                                y.toFloatOrNull() ?: 0f,
                                                z.toFloatOrNull() ?: 0f
                                            )
                                        )
                                    }
                                )
                            )
                            showSnackbar(Ext.ctx.getString(R.string.save_success))
                        }
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                    )
                }
            }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                TextField(
                    modifier = Modifier
                        .weight(2f)
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.large,
                    value = z,
                    onValueChange = { z = it },
                    label = { Text(text = "下降高度") },
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
                                .clickable { z = "" },
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = null,
                        )
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
                    singleLine = true,
                )
                FloatingActionButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    onClick = { softKeyboard?.hide() },
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Filled.MoveUp,
                        contentDescription = null,
                    )
                }
                FloatingActionButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    onClick = {
                        softKeyboard?.hide()
                        scope.launch {
                            val list = entity.data.toMutableList()
                            update(
                                entity.copy(
                                    data = list.map { point ->
                                        point.copy(
                                            axis = listOf(
                                                0f,
                                                y.toFloatOrNull() ?: 0f,
                                                z.toFloatOrNull() ?: 0f
                                            )
                                        )
                                    }
                                )
                            )
                            showSnackbar(Ext.ctx.getString(R.string.save_success))
                        }
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                    )
                }
            }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ContainerMainPagePreview() {
    ContainerMainPage(
        uiState = ContainerUiState(entities = listOf(ContainerEntity())),
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ContainerEditPagePreview() {
    val pointList = mutableListOf<Point>()
    repeat(6) {
        pointList.add(Point())
    }
    ContainerEditPage(entity = ContainerEntity(data = pointList))
}
