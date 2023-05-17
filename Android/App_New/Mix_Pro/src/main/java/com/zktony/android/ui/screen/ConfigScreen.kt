package com.zktony.android.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.zktony.android.ui.components.CustomTextField
import com.zktony.android.ui.components.ZkTonyScaffold
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.utils.PageEnum
import com.zktony.core.ext.Ext
import com.zktony.core.ext.format
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 系统配置
 *
 * @param modifier Modifier
 * @param navController NavHostController
 * @param viewModel ConfigViewModel
 * @return Unit
 */
@Composable
fun ConfigScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ConfigViewModel,
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
            ZkTonyTopAppBar(title = stringResource(id = R.string.system_config), navigation = {
                if (uiState.page == PageEnum.MAIN) {
                    navController.navigateUp()
                } else {
                    viewModel.navigationTo(PageEnum.MAIN)
                }
            })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        AnimatedVisibility(visible = uiState.page == PageEnum.MAIN) {
            ConfigMainPage(
                modifier = Modifier,
                uiState = uiState,
                navigationTo = viewModel::navigationTo,
            )
        }
        AnimatedVisibility(visible = uiState.page == PageEnum.TRAVEL_EDIT) {
            TravelEditPage(modifier = Modifier,
                navigationTo = viewModel::navigationTo,
                setTravel = viewModel::setTravel,
                travel = uiState.settings.travelList.ifEmpty { listOf(0f, 0f, 0f) },
                showSnackBar = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                })
        }
        AnimatedVisibility(visible = uiState.page == PageEnum.WASTE_EDIT) {
            WasteEditPage(modifier = Modifier,
                navigationTo = viewModel::navigationTo,
                setWaste = viewModel::setWaste,
                waste = uiState.settings.wasteList.ifEmpty { listOf(0f, 0f, 0f) },
                showSnackBar = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                })
        }
    }
}

@Composable
fun ConfigMainPage(
    modifier: Modifier = Modifier,
    navigationTo: (PageEnum) -> Unit = {},
    uiState: ConfigUiState,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium
            ),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            ElevatedCard(
                modifier = Modifier
                    .wrapContentHeight()
                    .clickable { navigationTo(PageEnum.TRAVEL_EDIT) },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(start = 16.dp),
                        painter = painterResource(id = R.drawable.ic_distance),
                        contentDescription = null,
                    )

                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.maximum_stroke),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    val list = uiState.settings.travelList.ifEmpty { listOf(0f, 0f, 0f) }
                    Text(
                        modifier = Modifier.padding(end = 16.dp),
                        text = "( ${list[0].format()} , ${list[1].format()} , ${list[2].format()} )",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        item {
            ElevatedCard(
                modifier = Modifier
                    .wrapContentHeight()
                    .clickable { navigationTo(PageEnum.WASTE_EDIT) },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(start = 16.dp),
                        painter = painterResource(id = R.drawable.ic_coordinate),
                        contentDescription = null,
                    )

                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.waste_tank),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    val list = uiState.settings.wasteList.ifEmpty { listOf(0f, 0f, 0f) }
                    Text(
                        modifier = Modifier.padding(end = 16.dp),
                        text = "( ${list[0].format()} , ${list[1].format()} , ${list[2].format()} )",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TravelEditPage(
    modifier: Modifier = Modifier,
    navigationTo: (PageEnum) -> Unit = {},
    setTravel: (Float, Float, Float) -> Unit = { _, _, _ -> },
    travel: List<Float> = listOf(0f, 0f, 0f),
    showSnackBar: (String) -> Unit = {},
) {
    var x by remember { mutableStateOf(travel[0].format()) }
    var y by remember { mutableStateOf(travel[1].format()) }
    var z by remember { mutableStateOf(travel[2].format()) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
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
                color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(128.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier
                    .size(84.dp)
                    .padding(end = 16.dp),
                painter = painterResource(id = R.drawable.ic_distance),
                contentDescription = null,
            )
            Text(
                text = "(",
                fontSize = 30.sp,
            )
            CustomTextField(
                modifier = Modifier
                    .width(128.dp)
                    .focusRequester(focusRequester),
                value = TextFieldValue(x.format(), TextRange(x.format().length)),
                onValueChange = { x = it.text },
                textStyle = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                })
            )
            Text(
                text = ",",
                fontSize = 30.sp,
            )
            CustomTextField(
                modifier = Modifier.width(128.dp),
                value = TextFieldValue(y.format(), TextRange(y.format().length)),
                onValueChange = { y = it.text },
                textStyle = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                })
            )
            Text(
                text = ",",
                fontSize = 30.sp,
            )
            CustomTextField(
                modifier = Modifier.width(128.dp),
                value = TextFieldValue(z.format(), TextRange(z.format().length)),
                onValueChange = { z = it.text },
                textStyle = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                keyboardActions = KeyboardActions(onDone = {
                    softKeyboard?.hide()
                })
            )
            Text(
                text = ")",
                fontSize = 30.sp,
            )
        }

        AnimatedVisibility(visible = travel[0].format() != x || travel[1].format() != y || travel[2].format() != z) {
            ElevatedButton(
                modifier = Modifier
                    .width(128.dp)
                    .padding(16.dp),
                onClick = {
                    setTravel(
                        x.toFloatOrNull() ?: 0f, y.toFloatOrNull() ?: 0f, z.toFloatOrNull() ?: 0f
                    )
                    navigationTo(PageEnum.MAIN)
                    showSnackBar(Ext.ctx.getString(R.string.save_success))
                },
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = stringResource(id = R.string.save),
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WasteEditPage(
    modifier: Modifier = Modifier,
    setWaste: (Float, Float, Float) -> Unit = { _, _, _ -> },
    waste: List<Float> = listOf(0f, 0f, 0f),
    navigationTo: (PageEnum) -> Unit = {},
    showSnackBar: (String) -> Unit = {},
) {
    var x by remember { mutableStateOf(waste[0].format()) }
    var y by remember { mutableStateOf(waste[1].format()) }
    var z by remember { mutableStateOf(waste[2].format()) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
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
                color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(128.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier
                    .size(84.dp)
                    .padding(end = 16.dp),
                painter = painterResource(id = R.drawable.ic_coordinate),
                contentDescription = null,
            )
            Text(
                text = "(",
                fontSize = 30.sp,
            )
            CustomTextField(
                modifier = Modifier
                    .width(128.dp)
                    .focusRequester(focusRequester),
                value = TextFieldValue(x.format(), TextRange(x.format().length)),
                onValueChange = { x = it.text },
                textStyle = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                })
            )
            Text(
                text = ",",
                fontSize = 30.sp,
            )
            CustomTextField(
                modifier = Modifier.width(128.dp),
                value = TextFieldValue(y.format(), TextRange(y.format().length)),
                onValueChange = { y = it.text },
                textStyle = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                })
            )
            Text(
                text = ",",
                fontSize = 30.sp,
            )
            CustomTextField(
                modifier = Modifier.width(128.dp),
                value = TextFieldValue(z.format(), TextRange(z.format().length)),
                onValueChange = { z = it.text },
                textStyle = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                keyboardActions = KeyboardActions(onDone = {
                    softKeyboard?.hide()
                })
            )
            Text(
                text = ")",
                fontSize = 30.sp,
            )
        }
        AnimatedVisibility(visible = waste[0].format() != x || waste[1].format() != y || waste[2].format() != z) {
            ElevatedButton(
                modifier = Modifier
                    .width(128.dp)
                    .padding(16.dp),
                onClick = {
                    setWaste(
                        x.toFloatOrNull() ?: 0f, y.toFloatOrNull() ?: 0f, z.toFloatOrNull() ?: 0f
                    )
                    navigationTo(PageEnum.MAIN)
                    showSnackBar(Ext.ctx.getString(R.string.save_success))
                },
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = stringResource(id = R.string.save),
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960)
fun ConfigMainPagePreview() {
    ConfigMainPage(
        uiState = ConfigUiState()
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960)
fun TravelEditPagePreview() {
    TravelEditPage()
}

@Composable
@Preview(showBackground = true, widthDp = 960)
fun WasteEditPagePreview() {
    WasteEditPage()
}