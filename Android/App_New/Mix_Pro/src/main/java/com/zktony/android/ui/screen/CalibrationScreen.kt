package com.zktony.android.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.zktony.android.data.entity.Calibration
import com.zktony.android.data.entity.CalibrationData
import com.zktony.android.ui.components.CustomTextField
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.viewmodel.CalibrationPage
import com.zktony.android.ui.viewmodel.CalibrationViewModel
import com.zktony.core.ext.format
import com.zktony.core.ext.simpleDateFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Calibration screen
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel CalibrationViewModel
 * @return Unit
 */
@Composable
fun CalibrationScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: CalibrationViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var active by remember { mutableStateOf(0L) }

    BackHandler {
        if (uiState.page == CalibrationPage.CALIBRATION) {
            navController.navigateUp()
        } else {
            viewModel.navigateTo(CalibrationPage.CALIBRATION)
        }
    }

    AnimatedVisibility(visible = uiState.page == CalibrationPage.CALIBRATION) {
        CalibrationPage(
            modifier = modifier,
            active = active,
            delete = viewModel::delete,
            enable = { viewModel.enable(active) },
            list = uiState.list,
            navigationTo = viewModel::navigateTo,
            setActive = { active = it },
        )
    }

    AnimatedVisibility(visible = uiState.page == CalibrationPage.CALIBRATION_ADD) {
        CalibrationAddPage(
            modifier = modifier,
            insert = viewModel::insert,
            list = uiState.list,
            navigationTo = viewModel::navigateTo,
            setActive = { active = it },
        )
    }

    AnimatedVisibility(visible = uiState.page == CalibrationPage.CALIBRATION_EDIT) {
        CalibrationEditPage(
            modifier = modifier,
            active = active,
            addLiquid = viewModel::addLiquid,
            delete = { viewModel.deleteData(active, it) },
            insert = viewModel::insertData,
            list = viewModel.dataList(active),
            list1 = viewModel.calculatorList(active),
            navigationTo = viewModel::navigateTo,
        )
    }
}

/**
 * CalibrationPage
 *
 * @param modifier Modifier
 * @param active Long
 * @param delete Function1<Long, Unit>
 * @param enable Function0<Unit>
 * @param list List<Calibration>
 * @param navigationTo Function1<CalibrationPage, Unit>
 * @param setActive Function1<Long, Unit>
 * @return Unit
 */
@Composable
fun CalibrationPage(
    modifier: Modifier = Modifier,
    active: Long = 0L,
    delete: (Long) -> Unit = {},
    enable: () -> Unit = {},
    list: List<Calibration>,
    navigationTo: (CalibrationPage) -> Unit = {},
    setActive: (Long) -> Unit = {},
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
                        val background = if (active == it.id) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        Card(
                            modifier = Modifier
                                .wrapContentHeight()
                                .clickable { setActive(it.id) },
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
                                    imageVector = if (it.active == 1) Icons.Filled.Check else Icons.Filled.Circle,
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
                onClick = { navigationTo(CalibrationPage.CALIBRATION_ADD) }
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            }
            AnimatedVisibility(visible = active != 0L) {

                var count by remember { mutableStateOf(0) }

                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {
                        count++
                        if (count == 2) {
                            delete(active)
                            setActive(0L)
                            count = 0
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
            AnimatedVisibility(visible = active != 0L) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = { navigationTo(CalibrationPage.CALIBRATION_EDIT) }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                    )
                }
            }
            AnimatedVisibility(visible = active != 0L) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = { enable() }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

/**
 * CalibrationAddPage
 *
 * @param modifier Modifier
 * @param insert Function1<Calibration, Unit>
 * @param list List<Calibration>
 * @param navigationTo Function1<CalibrationPage, Unit>
 * @param setActive Function1<Long, Unit>
 * @return Unit
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CalibrationAddPage(
    modifier: Modifier = Modifier,
    insert: (Calibration) -> Unit = {},
    list: List<Calibration>,
    navigationTo: (CalibrationPage) -> Unit = {},
    setActive: (Long) -> Unit = {},
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
                navigationTo(CalibrationPage.CALIBRATION)
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
                        val entity = Calibration(name = name)
                        insert(entity)
                        setActive(entity.id)
                        navigationTo(CalibrationPage.CALIBRATION_EDIT)
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

/**
 * CalibrationEditPage
 *
 * @param modifier Modifier
 * @param active Long
 * @param addLiquid Function2<Int, Float, Unit>
 * @param delete Function1<Long, Unit>
 * @param insert Function1<CalibrationData, Unit>
 * @param list1 Flow<List<Pair<Int, Float>>>
 * @param list Flow<List<CalibrationData>>
 * @param navigationTo Function1<CalibrationPage, Unit>
 * @return Unit
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CalibrationEditPage(
    modifier: Modifier = Modifier,
    active: Long = 0L,
    addLiquid: (Int, Float) -> Unit = { _, _ -> },
    delete: (Long) -> Unit = {},
    insert: (CalibrationData) -> Unit = {},
    list1: Flow<List<Pair<Int, Float>>> = flowOf(),
    list: Flow<List<CalibrationData>> = flowOf(),
    navigationTo: (CalibrationPage) -> Unit = {},
) {

    val data by list.collectAsStateWithLifecycle(initialValue = emptyList())
    val data1 by list1.collectAsStateWithLifecycle(initialValue = emptyList())
    var expand by remember { mutableStateOf(false) }
    var index by remember { mutableStateOf(0) }
    var expect by remember { mutableStateOf("") }
    var actual by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val softKeyboard = LocalSoftwareKeyboardController.current

    Column {
        Column(
            modifier = modifier
                .padding(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ZkTonyTopAppBar(
                title = stringResource(id = R.string.edit),
                onBack = {
                    navigationTo(CalibrationPage.CALIBRATION)
                })
        }

        Row(
            modifier = Modifier
                .weight(4f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = modifier
                    .weight(3f)
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = MaterialTheme.shapes.medium
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    data.forEach {
                        item {
                            Card(
                                modifier = Modifier.wrapContentHeight(),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Image(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(start = 16.dp),
                                        painter = painterResource(id = R.drawable.ic_note),
                                        contentDescription = null,
                                    )

                                    Text(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = "V${it.index + 1}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = it.expect.format(2),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = it.actual.format(2),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = "${(it.percent * 100).format(2)}%",
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    IconButton(
                                        modifier = Modifier.size(48.dp),
                                        onClick = {
                                            delete(it.id)
                                        }
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(36.dp),
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color.Red,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Column(
                modifier = modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = MaterialTheme.shapes.medium
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    data1.forEach {
                        item {
                            Card(
                                modifier = Modifier.wrapContentHeight(),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Image(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(start = 16.dp),
                                        painter = painterResource(id = R.drawable.ic_calculator),
                                        contentDescription = null,
                                    )

                                    Text(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = "V${it.first + 1}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = it.second.format(2),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(visible = !expand) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FloatingActionButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = { expand = true }
                    ) {
                        Text(
                            text = "V${index + 1}",
                            style = TextStyle(fontSize = 24.sp),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CustomTextField(
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            value = TextFieldValue(expect, TextRange(expect.length)),
                            hint = stringResource(id = R.string.expect),
                            onValueChange = { expect = it.text },
                            textStyle = TextStyle(
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                            ),
                            hiltTextStyle = TextStyle(
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    softKeyboard?.hide()
                                }
                            ),
                        )
                        Divider(thickness = 2.dp)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CustomTextField(
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            value = TextFieldValue(actual, TextRange(actual.length)),
                            hint = stringResource(id = R.string.actual),
                            onValueChange = { actual = it.text },
                            textStyle = TextStyle(
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                            ),
                            hiltTextStyle = TextStyle(
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    softKeyboard?.hide()
                                }
                            ),
                        )
                        Divider(thickness = 2.dp)
                    }

                    FloatingActionButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = { addLiquid(index, expect.toFloatOrNull() ?: 0f) }
                    ) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.MoveUp,
                            contentDescription = null,
                        )
                    }

                    AnimatedVisibility(visible = expect.isNotEmpty() && actual.isNotEmpty()) {
                        FloatingActionButton(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            onClick = {
                                insert(
                                    CalibrationData(
                                        subId = active,
                                        index = index,
                                        expect = expect.toFloatOrNull() ?: 0f,
                                        actual = actual.toFloatOrNull() ?: 0f,
                                    )
                                )
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(36.dp),
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(visible = expand) {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    (0..8).forEach {
                        item {
                            FloatingActionButton(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onClick = {
                                    index = it
                                    expand = false
                                }
                            ) {
                                Text(
                                    text = "V${it + 1}",
                                    style = TextStyle(fontSize = 24.sp),
                                )
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
fun CalibrationPagePreview() {
    CalibrationPage(
        list = listOf(Calibration())
    )
}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationAddPagePreview() {
    CalibrationAddPage(list = emptyList())
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationEditPagePreview() {
    CalibrationEditPage()
}