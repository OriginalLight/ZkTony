package com.zktony.android.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.zktony.android.data.entity.Container
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.viewmodel.ContainerPage
import com.zktony.android.ui.viewmodel.ContainerViewModel
import com.zktony.core.ext.simpleDateFormat
import kotlinx.coroutines.delay

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
    var selected by remember { mutableStateOf(0L) }

    BackHandler {
        if (uiState.page == ContainerPage.CONTAINER) {
            navController.navigateUp()
        } else {
            viewModel.navigationTo(ContainerPage.CONTAINER)
        }
    }

    AnimatedVisibility(visible = uiState.page == ContainerPage.CONTAINER) {
        ContainerPage(
            modifier = modifier,
            delete = viewModel::delete,
            list = uiState.list,
            navigationTo = viewModel::navigationTo,
            selected = selected,
            toggleSelected = { selected = if (selected == it) 0L else it },
        )
    }

    AnimatedVisibility(visible = uiState.page == ContainerPage.CONTAINER_ADD) {
        ContainerAddPage(
            modifier = modifier,
            insert = viewModel::insert,
            list = uiState.list,
            navigationTo = viewModel::navigationTo,
            toggleSelected = { selected = it },
        )
    }
}



@Composable
fun ContainerPage(
    modifier: Modifier = Modifier,
    delete: (Long) -> Unit = {},
    list: List<Container>,
    navigationTo: (ContainerPage) -> Unit = {},
    selected: Long = 0L,
    toggleSelected: (Long) -> Unit = {},
) {
    val columnState = rememberLazyListState()

    Row {
        Column(
            modifier = modifier
                .weight(6f)
                .fillMaxHeight()
                .padding(start = 8.dp, top = 8.dp, end = 4.dp, bottom = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp),
                state = columnState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                list.forEach {
                    item {
                        val background = if (selected == it.id) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        Card(
                            modifier = Modifier
                                .wrapContentHeight()
                                .clickable { toggleSelected(it.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = background,
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(start = 16.dp),
                                    imageVector = Icons.Outlined.Water,
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
        }
        Column(
            modifier = modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 4.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
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
                onClick = { navigationTo(ContainerPage.CONTAINER_ADD) }
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add),
                )
            }
            AnimatedVisibility(visible = selected != 0L) {

                var count by remember { mutableStateOf(0) }

                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {
                        count++
                        if (count == 2) {
                            delete(selected)
                            toggleSelected(0L)
                            count = 0
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
            AnimatedVisibility(visible = selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = { navigationTo(ContainerPage.CONTAINER_EDIT) }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.edit),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ContainerAddPage(
    modifier: Modifier = Modifier,
    insert: (Container) -> Unit = {},
    list: List<Container>,
    navigationTo: (ContainerPage) -> Unit = {},
    toggleSelected: (Long) -> Unit = {},
) {
    var name by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val softKeyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(100) //延迟操作(关键点)
        focusRequester.requestFocus()
        softKeyboard?.show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ZkTonyTopAppBar(
            title = stringResource(id = R.string.add),
            onBack = {
                navigationTo(ContainerPage.CONTAINER)
            })
        Spacer(modifier = Modifier.height(128.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 128.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                shape = MaterialTheme.shapes.large,
                value = name,
                onValueChange = { name = it },
                textStyle = TextStyle(fontSize = 24.sp),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                leadingIcon = {
                    Icon(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(horizontal = 8.dp),
                        imageVector = Icons.Default.Abc,
                        contentDescription = null,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        softKeyboard?.hide()
                    }
                ),
                singleLine = true,
            )
            AnimatedVisibility(visible = name.isNotEmpty() && !list.any { it.name == name }) {
                FilledIconButton(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(48.dp),
                    onClick = {
                        val entity = Container(name = name)
                        insert(entity)
                        toggleSelected(entity.id)
                        navigationTo(ContainerPage.CONTAINER)
                        softKeyboard?.hide()
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ContainerPagePreview() {
    ContainerPage(list = listOf(Container()))
}
