package com.zktony.android.ui.screen.calibration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
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
import com.zktony.core.ext.format
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun CalibrationEditPage(
    modifier: Modifier = Modifier,
    addLiquid: (Int, Float) -> Unit = { _, _ -> },
    entity: Calibration = Calibration(),
    update: (Calibration) -> Unit = { },
) {

    var expand by remember { mutableStateOf(false) }
    var index by remember { mutableStateOf(0) }
    var expect by remember { mutableStateOf("") }
    var actual by remember { mutableStateOf("") }
    val softKeyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.windowInsetsPadding(WindowInsets.imeAnimationSource)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            entity.data.compute().forEach { (index, avg, list) ->
                item {
                    Card {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier.padding(start = 16.dp),
                                    text = "V${index + 1}",
                                    style = MaterialTheme.typography.titleLarge,
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    modifier = Modifier.padding(end = 16.dp),
                                    text = "${(100f * avg).format(2)} μL",
                                    style = TextStyle(
                                        fontSize = 24.sp,
                                        fontStyle = FontStyle.Italic,
                                    ),
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
                                        AssistChip(onClick = {
                                            scope.launch {
                                                val l1 = entity.data.toMutableList()
                                                l1.remove(it1)
                                                update(
                                                    entity.copy(
                                                        data = l1
                                                    )
                                                )
                                            }
                                        }, label = {
                                            Text(
                                                text = "${it1.actual.format(2)} / ${
                                                    it1.expect.format(
                                                        2
                                                    )
                                                }"
                                            )
                                        }, trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color.Red,
                                            )
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .height(128.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
        ) {
            AnimatedVisibility(visible = !expand) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .width(128.dp)
                            .padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.small,
                        onClick = { expand = true }) {
                        Text(
                            text = "V${index + 1}",
                            style = TextStyle(fontSize = 24.sp),
                        )
                    }

                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.large,
                        value = TextFieldValue(expect, TextRange(expect.length)),
                        onValueChange = { expect = it.text },
                        placeholder = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(id = R.string.expect),
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                            )
                        },
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                        ),
                        trailingIcon = {
                            Icon(
                                modifier = Modifier.clickable { expect = "" },
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = null,
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            softKeyboard?.hide()
                        }),
                        singleLine = true,
                    )

                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.large,
                        value = TextFieldValue(actual, TextRange(actual.length)),
                        onValueChange = { actual = it.text },
                        placeholder = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(id = R.string.actual),
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                            )
                        },
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                        ),
                        trailingIcon = {
                            Icon(
                                modifier = Modifier.clickable { actual = "" },
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = null,
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            softKeyboard?.hide()
                        }),
                        singleLine = true,
                    )

                    FloatingActionButton(
                        modifier = Modifier
                            .width(196.dp)
                            .padding(horizontal = 16.dp), onClick = {
                            softKeyboard?.hide()
                            addLiquid(index, expect.toFloatOrNull() ?: 0f)
                        }) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.MoveUp,
                            contentDescription = null,
                        )
                    }

                    AnimatedVisibility(visible = expect.isNotEmpty() && actual.isNotEmpty()) {
                        FloatingActionButton(modifier = Modifier
                            .width(196.dp)
                            .padding(horizontal = 16.dp),
                            onClick = {
                                softKeyboard?.hide()
                                scope.launch {
                                    val l1 = entity.data.toMutableList()
                                    val data = CalibrationData(
                                        index = index,
                                        expect = expect.toFloatOrNull() ?: 0f,
                                        actual = actual.toFloatOrNull() ?: 0f,
                                    )
                                    l1.add(data)
                                    update(entity.copy(data = l1))
                                }
                            }) {
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(13) {
                        item {
                            OutlinedButton(modifier = Modifier
                                .width(128.dp)
                                .padding(horizontal = 16.dp),
                                shape = MaterialTheme.shapes.small,
                                onClick = {
                                    index = it
                                    expand = false
                                }) {
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