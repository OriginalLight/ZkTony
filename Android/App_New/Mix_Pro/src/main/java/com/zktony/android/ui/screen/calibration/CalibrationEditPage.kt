package com.zktony.android.ui.screen.calibration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataSaverOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.zktony.android.R
import com.zktony.android.core.ext.compute
import com.zktony.android.data.entity.Calibration
import com.zktony.android.data.entity.CalibrationData
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.navigation.PageEnum
import com.zktony.core.ext.format

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalibrationEditPage(
    modifier: Modifier = Modifier,
    addLiquid: (Int, Float) -> Unit = { _, _ -> },
    entity: Calibration = Calibration(),
    navigationTo: (PageEnum) -> Unit = {},
    update: (Calibration) -> Unit = { },
) {

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
                    navigationTo(PageEnum.MAIN)
                }
            )
        }

        Column(
            modifier = modifier
                .weight(4f)
                .fillMaxWidth()
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
                entity.data.compute().forEach { (index, avg, list) ->
                    item {
                        Card(
                            modifier = Modifier.wrapContentHeight(),
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .padding(start = 8.dp),
                                        imageVector = Icons.Default.DataSaverOff,
                                        contentDescription = null
                                    )

                                    Text(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = "V${index + 1}",
                                        style = MaterialTheme.typography.titleLarge,
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Text(
                                        modifier = Modifier.padding(end = 8.dp),
                                        text = "${(avg * 100).format(2)} %",
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }

                                LazyRow(
                                    modifier = modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    list.forEach { it1 ->
                                        item {
                                            AssistChip(
                                                onClick = {
                                                    val l1 = entity.data.toMutableList()
                                                    l1.remove(it1)
                                                    update(
                                                        entity.copy(
                                                            data = l1
                                                        )
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = "${it1.actual.format(2)} / ${
                                                            it1.expect.format(
                                                                2
                                                            )
                                                        }"
                                                    )
                                                },
                                                trailingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = null,
                                                        tint = Color.Red,
                                                    )
                                                }
                                            )
                                        }
                                    }
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
                        modifier = Modifier
                            .width(128.dp)
                            .padding(horizontal = 16.dp),
                        onClick = { expand = true }
                    ) {
                        Text(
                            text = "V${index + 1}",
                            style = TextStyle(fontSize = 24.sp),
                        )
                    }

                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .focusRequester(focusRequester),
                        shape = MaterialTheme.shapes.large,
                        value = TextFieldValue(expect, TextRange(expect.length)),
                        onValueChange = { expect = it.text },
                        label = { Text(text = stringResource(id = R.string.expect)) },
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center
                        ),
                        trailingIcon = {
                            Icon(
                                modifier = Modifier
                                    .clickable { expect = "" },
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = null,
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                softKeyboard?.hide()
                            }
                        ),
                        singleLine = true,
                    )

                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .focusRequester(focusRequester),
                        shape = MaterialTheme.shapes.large,
                        value = TextFieldValue(actual, TextRange(actual.length)),
                        onValueChange = { actual = it.text },
                        label = { Text(text = stringResource(id = R.string.actual)) },
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center
                        ),
                        trailingIcon = {
                            Icon(
                                modifier = Modifier
                                    .clickable { actual = "" },
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = null,
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
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
                            .width(128.dp)
                            .padding(horizontal = 16.dp),
                        onClick = {
                            softKeyboard?.hide()
                            addLiquid(index, expect.toFloatOrNull() ?: 0f)
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.MoveUp,
                            contentDescription = null,
                        )
                    }

                    AnimatedVisibility(visible = expect.isNotEmpty() && actual.isNotEmpty()) {
                        FloatingActionButton(
                            modifier = Modifier
                                .width(128.dp)
                                .padding(horizontal = 16.dp),
                            onClick = {
                                softKeyboard?.hide()
                                val l1 = entity.data.toMutableList()
                                val data = CalibrationData(
                                    index = index,
                                    expect = expect.toFloatOrNull() ?: 0f,
                                    actual = actual.toFloatOrNull() ?: 0f,
                                )
                                l1.add(data)
                                update(entity.copy(data = l1))
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(36.dp),
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(id = R.string.save),
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
                    (0..12).forEach {
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
fun CalibrationEditPagePreview() {
    CalibrationEditPage()
}