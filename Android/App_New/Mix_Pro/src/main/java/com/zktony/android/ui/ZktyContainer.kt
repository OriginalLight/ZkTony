package com.zktony.android.ui

import android.graphics.Point
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.zktony.android.ui.components.CustomTextField
import com.zktony.android.ui.components.DynamicMixPlate
import com.zktony.android.ui.components.InputDialog
import com.zktony.android.ui.components.ZktyTopAppBar
import com.zktony.android.ui.utils.PageType
import com.zktony.core.ext.format
import com.zktony.core.ext.showShortToast
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
fun ZktyContainer(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ZktyContainerViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val page = remember { mutableStateOf(PageType.LIST) }

    BackHandler {
        when (page.value) {
            PageType.LIST -> navController.navigateUp()
            else -> page.value = PageType.LIST
        }
    }

    Column(modifier = Modifier) {
        // app bar for edit page
        AnimatedVisibility(visible = page.value == PageType.EDIT) {
            ZktyTopAppBar(
                title = stringResource(id = R.string.edit),
                navigation = {
                    when (page.value) {
                        PageType.LIST -> navController.navigateUp()
                        else -> page.value = PageType.LIST
                    }
                }
            )
        }
        // list page
        AnimatedVisibility(visible = page.value == PageType.LIST) {
            ContainerList(
                modifier = modifier,
                uiState = uiState,
                insert = viewModel::insert,
                delete = viewModel::delete,
                navigationToEdit = { page.value = PageType.EDIT },
                toggleSelected = viewModel::toggleSelected,
            )
        }
        // edit page
        AnimatedVisibility(visible = page.value == PageType.EDIT) {
            ContainerEdit(
                modifier = modifier,
                entity = uiState.entities.find { it.id == uiState.selected }!!,
                update = viewModel::update,
            )
        }
    }
}

@Composable
fun ContainerList(
    modifier: Modifier = Modifier,
    uiState: ContainerUiState = ContainerUiState(),
    insert: (String) -> Unit = {},
    delete: (Long) -> Unit = {},
    navigationToEdit: () -> Unit = {},
    toggleSelected: (Long) -> Unit = {},
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
                        insert(it)
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
                                text = it.text,
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
            // Add
            FloatingActionButton(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                onClick = { showDialog = true },
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
                var count by remember { mutableIntStateOf(0) }

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
            // Edit
            AnimatedVisibility(visible = uiState.selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = navigationToEdit,
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
fun ContainerEdit(
    modifier: Modifier = Modifier,
    entity: ContainerEntity = ContainerEntity(),
    update: (ContainerEntity) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = MaterialTheme.shapes.medium,
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.weight(0.5f))
                    // 画竖线
                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(),
                        color = Color.Black,
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.moving_distance),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                    )
                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(),
                        color = Color.Black,
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.actions),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                    )
                }
                Divider(color = Color.Black)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(0.5f),
                        text = stringResource(id = R.string.y_axis),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                    )
                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(),
                        color = Color.Black,
                    )
                    CustomTextField(
                        modifier = Modifier.weight(1f),
                        value = TextFieldValue(y, TextRange(y.length)),
                        onValueChange = {
                            scope.launch {
                                y = it.text
                                val axis = entity.axis.toMutableList()
                                axis[0] = y.toFloatOrNull() ?: 0f
                                update(entity.copy(axis = axis))
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
                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(),
                        color = Color.Black,
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(
                            modifier = Modifier.width(96.dp),
                            onClick = { keyboard?.hide() },
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
                            )
                        }
                    }
                }
                Divider(color = Color.Black)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(0.5f),
                        text = stringResource(id = R.string.z_axis),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                    )
                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(),
                        color = Color.Black,
                    )
                    CustomTextField(
                        modifier = Modifier.weight(1f),
                        value = TextFieldValue(z, TextRange(z.length)),
                        onValueChange = {
                            scope.launch {
                                z = it.text
                                val axis = entity.axis.toMutableList()
                                axis[1] = z.toFloatOrNull() ?: 0f
                                update(entity.copy(axis = axis))
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
                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(),
                        color = Color.Black,
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(
                            modifier = Modifier.width(96.dp),
                            onClick = { keyboard?.hide() },
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
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
fun ContainerListPreview() {
    ContainerList(
        uiState = ContainerUiState(entities = listOf(ContainerEntity())),
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ContainerEditPreview() {
    val pointList = mutableListOf<Point>()
    repeat(6) {
        pointList.add(Point())
    }
    ContainerEdit(entity = ContainerEntity())
}
