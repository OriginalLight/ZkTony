package com.zktony.android.ui.screen.container

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.WidthNormal
import androidx.compose.material3.Divider
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.R
import com.zktony.android.data.entity.Container
import com.zktony.android.ui.components.DynamicMixPlate
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.navigation.PageEnum
import com.zktony.core.ext.format

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContainerEditPage(
    modifier: Modifier = Modifier,
    entity: Container = Container(),
    navigationTo: (PageEnum) -> Unit = {},
    update: (Container) -> Unit = {},
) {
    val softKeyboard = LocalSoftwareKeyboardController.current

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
            title = stringResource(id = R.string.edit),
            onBack = { navigationTo(PageEnum.MAIN) })

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DynamicMixPlate(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(32.dp),
                count = entity.data.size,
            )

            AnimatedVisibility(visible = entity.data.isNotEmpty()) {
                if (entity.data.isNotEmpty()) {
                    var y by remember { mutableStateOf(entity.data[0].y.format(2)) }
                    var z by remember { mutableStateOf(entity.data[0].z.format(2)) }

                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .weight(2f)
                                    .padding(horizontal = 16.dp),
                                shape = MaterialTheme.shapes.large,
                                value = y,
                                onValueChange = { y = it },
                                label = { Text(text = "容器位置") },
                                textStyle = TextStyle(
                                    textAlign = TextAlign.Center
                                ),
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(horizontal = 8.dp),
                                        imageVector = Icons.Outlined.WidthNormal,
                                        contentDescription = null,
                                    )
                                },
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
                                    val list = entity.data.toMutableList()
                                    update(
                                        entity.copy(
                                            data = list.map { point ->
                                                point.copy(
                                                    y = y.toFloatOrNull() ?: 0f,
                                                    z = z.toFloatOrNull() ?: 0f,
                                                )
                                            }
                                        )
                                    )
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .weight(2f)
                                    .padding(horizontal = 16.dp),
                                shape = MaterialTheme.shapes.large,
                                value = z,
                                onValueChange = { z = it },
                                label = { Text(text = "下降高度") },
                                textStyle = TextStyle(
                                    textAlign = TextAlign.Center
                                ),
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(horizontal = 8.dp),
                                        imageVector = Icons.Outlined.Height,
                                        contentDescription = null,
                                    )
                                },
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
                                    val list = entity.data.toMutableList()
                                    update(
                                        entity.copy(
                                            data = list.map { point ->
                                                point.copy(
                                                    y = y.toFloatOrNull() ?: 0f,
                                                    z = z.toFloatOrNull() ?: 0f,
                                                )
                                            }
                                        )
                                    )
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
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ContainerEditPagePreview() {
    ContainerEditPage(entity = Container())
}
