package com.zktony.android.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.viewmodel.ConfigPage
import com.zktony.android.ui.viewmodel.ConfigUiState
import com.zktony.android.ui.viewmodel.ConfigViewModel
import com.zktony.core.ext.format
import kotlinx.coroutines.delay

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

    BackHandler {
        if (uiState.page == ConfigPage.CONFIG) {
            navController.navigateUp()
        } else {
            viewModel.navigateTo(ConfigPage.CONFIG)
        }
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
            title = stringResource(id = R.string.system_config),
            onBack = {
                if (uiState.page == ConfigPage.CONFIG) {
                    navController.navigateUp()
                } else {
                    viewModel.navigateTo(ConfigPage.CONFIG)
                }
            }
        )
        AnimatedVisibility(visible = uiState.page == ConfigPage.CONFIG) {
            ConfigPage(
                modifier = Modifier,
                uiState = uiState,
                navigationTo = viewModel::navigateTo
            )
        }
        AnimatedVisibility(visible = uiState.page == ConfigPage.TRAVEL_EDIT) {
            TravelEditPage(
                modifier = Modifier,
                uiState = uiState,
                setTravel = { x, y, z ->
                    viewModel.setTravel(x, y, z)
                    viewModel.navigateTo(ConfigPage.CONFIG)
                },
            )
        }

        AnimatedVisibility(visible = uiState.page == ConfigPage.WASTE_EDIT) {
            WasteEditPage(
                modifier = Modifier,
                uiState = uiState,
                setWaste = { x, y, z ->
                    viewModel.setWaste(x, y, z)
                    viewModel.navigateTo(ConfigPage.CONFIG)
                },
            )
        }
    }
}

/**
 * ConfigPage
 *
 * @param modifier Modifier
 * @param navigationTo Function1<ConfigPage, Unit>
 * @param uiState ConfigUiState
 * @return Unit
 */
@Composable
fun ConfigPage(
    modifier: Modifier = Modifier,
    navigationTo: (ConfigPage) -> Unit = {},
    uiState: ConfigUiState,
) {

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        columns = GridCells.Fixed(2)
    ) {
        item {
            Card(
                modifier = Modifier
                    .wrapContentHeight()
                    .clickable { navigationTo(ConfigPage.TRAVEL_EDIT) },
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
                    Text(
                        modifier = Modifier.padding(end = 16.dp),
                        text = "( ${uiState.xAxisTravel.format()} , ${uiState.yAxisTravel.format()} , ${uiState.zAxisTravel.format()} )",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .wrapContentHeight()
                    .clickable { navigationTo(ConfigPage.WASTE_EDIT) },
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
                    Text(
                        modifier = Modifier.padding(end = 16.dp),
                        text = "( ${uiState.wasteX.format()} , ${uiState.wasteY.format()} , ${uiState.wasteZ.format()} )",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

/**
 * TravelEditPage
 *
 * @param modifier Modifier
 * @param setTravel Function3<Float, Float, Float, Unit>
 * @param uiState ConfigUiState
 * @return Unit
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TravelEditPage(
    modifier: Modifier = Modifier,
    setTravel: (Float, Float, Float) -> Unit = { _, _, _ -> },
    uiState: ConfigUiState,
) {
    var x by remember { mutableStateOf(uiState.xAxisTravel.format()) }
    var y by remember { mutableStateOf(uiState.yAxisTravel.format()) }
    var z by remember { mutableStateOf(uiState.zAxisTravel.format()) }
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
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.maximum_stroke),
            style = MaterialTheme.typography.titleLarge
        )
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
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
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
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
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
        AnimatedVisibility(visible = uiState.xAxisTravel.format() != x || uiState.yAxisTravel.format() != y || uiState.zAxisTravel.format() != z) {
            Button(
                modifier = Modifier.padding(vertical = 16.dp),
                onClick = {
                    setTravel(
                        x.toFloatOrNull() ?: 0f,
                        y.toFloatOrNull() ?: 0f,
                        z.toFloatOrNull() ?: 0f
                    )
                },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Save,
                        contentDescription = stringResource(id = R.string.save),
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(id = R.string.save),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }
}

/**
 * WasteEditPage
 *
 * @param modifier Modifier
 * @param setWaste Function3<Float, Float, Float, Unit>
 * @param uiState ConfigUiState
 * @return Unit
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WasteEditPage(
    modifier: Modifier = Modifier,
    setWaste: (Float, Float, Float) -> Unit = { _, _, _ -> },
    uiState: ConfigUiState,
) {
    var x by remember { mutableStateOf(uiState.wasteX.format()) }
    var y by remember { mutableStateOf(uiState.wasteY.format()) }
    var z by remember { mutableStateOf(uiState.wasteZ.format()) }
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
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.waste_tank),
            style = MaterialTheme.typography.titleLarge
        )
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
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
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
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
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
        AnimatedVisibility(visible = uiState.wasteX.format() != x || uiState.wasteY.format() != y || uiState.wasteZ.format() != z) {
            Button(
                modifier = Modifier.padding(vertical = 16.dp),
                onClick = {
                    setWaste(
                        x.toFloatOrNull() ?: 0f,
                        y.toFloatOrNull() ?: 0f,
                        z.toFloatOrNull() ?: 0f
                    )
                },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Save,
                        contentDescription = stringResource(id = R.string.save),
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(id = R.string.save),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960)
fun ConfigPagePreview() {
    ConfigPage(
        uiState = ConfigUiState(
            xAxisTravel = 0f,
            yAxisTravel = 0f,
            zAxisTravel = 0f,
        )
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960)
fun TravelEditPagePreview() {
    TravelEditPage(
        uiState = ConfigUiState()
    )
}

@Composable
@Preview(showBackground = true, widthDp = 960)
fun WasteEditPagePreview() {
    WasteEditPage(
        uiState = ConfigUiState()
    )
}