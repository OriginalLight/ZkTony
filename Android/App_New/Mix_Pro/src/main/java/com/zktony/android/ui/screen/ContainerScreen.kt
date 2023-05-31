package com.zktony.android.ui.screen

import android.graphics.Point
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.zktony.android.logic.data.entities.ContainerEntity
import com.zktony.android.ui.components.DynamicMixPlate
import com.zktony.android.ui.components.ZkTonyBottomAddAppBar
import com.zktony.android.ui.components.ZkTonyScaffold
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.utils.PageEnum
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
            ContainerMainPage(
                modifier = modifier,
                uiState = uiState,
                delete = viewModel::delete,
                navigationTo = viewModel::navigationTo,
                toggleSelected = viewModel::toggleSelected,
            )
        }
        AnimatedVisibility(visible = uiState.page == PageEnum.EDIT) {
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
                    Card(
                        modifier = Modifier
                            .height(48.dp)
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Image(
                                modifier = Modifier.size(32.dp),
                                painter = painterResource(id = R.drawable.ic_module),
                                contentDescription = null,
                            )
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
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
                    contentDescription = null,
                    tint = Color.Black,
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
                        tint = Color.Black,
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
    var y by remember { mutableStateOf(entity.axis[0].format(1)) }
    var z by remember { mutableStateOf(entity.axis[1].format(1)) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.imeAnimationSource)
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium,
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
                    count = 6,
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    value = TextFieldValue(text = y, selection = TextRange(y.length)),
                    onValueChange = { y = it.text },
                    leadingIcon = {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "容器位置"
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                    ),
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

                Button(
                    modifier = Modifier.width(156.dp),
                    onClick = { softKeyboard?.hide() },
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                    )
                }
                Button(
                    modifier = Modifier.width(156.dp),
                    enabled = (y.toFloatOrNull() ?: 0f) != entity.axis[0],
                    onClick = {
                        softKeyboard?.hide()
                        scope.launch {
                            update(
                                entity.copy(
                                    axis = listOf(
                                        y.toFloatOrNull() ?: 0f,
                                        z.toFloatOrNull() ?: 0f
                                    )
                                )
                            )
                            showSnackbar(Ext.ctx.getString(R.string.save_success))
                        }
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    value = TextFieldValue(text = z, selection = TextRange(z.length)),
                    onValueChange = { z = it.text },
                    leadingIcon = {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "下降高度"
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                    ),
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
                Button(
                    modifier = Modifier.width(156.dp),
                    onClick = { softKeyboard?.hide() },
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                    )
                }
                Button(
                    modifier = Modifier.width(156.dp),
                    enabled = (z.toFloatOrNull() ?: 0f) != entity.axis[1],
                    onClick = {
                        softKeyboard?.hide()
                        scope.launch {
                            update(
                                entity.copy(
                                    axis = listOf(
                                        y.toFloatOrNull() ?: 0f,
                                        z.toFloatOrNull() ?: 0f
                                    )
                                )
                            )
                            showSnackbar(Ext.ctx.getString(R.string.save_success))
                        }
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
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
    ContainerEditPage(entity = ContainerEntity())
}
