package com.zktony.android.ui.screen.container

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.WidthNormal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.R
import com.zktony.android.data.entity.Container
import com.zktony.android.ui.components.DynamicMixPlate
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.core.ext.format
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * ContainerEditPage
 *
 * @param modifier Modifier
 * @param entityFlow Flow<Container>
 * @param navigationTo Function1<ContainerPage, Unit>
 * @param update Function1<Container, Unit>
 * @return Unit
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContainerEditPage(
    modifier: Modifier = Modifier,
    entityFlow: Flow<Container> = emptyFlow(),
    navigationTo: (ContainerPage) -> Unit = {},
    update: (Container) -> Unit = {},
) {
    val entity by entityFlow.collectAsStateWithLifecycle(initialValue = Container())
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
            onBack = { navigationTo(ContainerPage.CONTAINER) })

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
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.large,
                                value = y,
                                onValueChange = { y = it },
                                label = { Text(text = "容器位置") },
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(horizontal = 8.dp),
                                        imageVector = Icons.Outlined.WidthNormal,
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
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FloatingActionButton(
                                    modifier = Modifier
                                        .width(128.dp)
                                        .padding(16.dp),
                                    onClick = { },
                                ) {
                                    Icon(
                                        modifier = Modifier.size(36.dp),
                                        imageVector = Icons.Filled.MoveUp,
                                        contentDescription = null,
                                    )
                                }
                                FloatingActionButton(
                                    modifier = Modifier
                                        .width(128.dp)
                                        .padding(16.dp),
                                    onClick = {
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
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.large,
                                value = z,
                                onValueChange = { z = it },
                                label = { Text(text = "下降高度") },
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(horizontal = 8.dp),
                                        imageVector = Icons.Outlined.Height,
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
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FloatingActionButton(
                                    modifier = Modifier
                                        .width(128.dp)
                                        .padding(16.dp),
                                    onClick = { },
                                ) {
                                    Icon(
                                        modifier = Modifier.size(36.dp),
                                        imageVector = Icons.Filled.MoveUp,
                                        contentDescription = null,
                                    )
                                }
                                FloatingActionButton(
                                    modifier = Modifier
                                        .width(128.dp)
                                        .padding(16.dp),
                                    onClick = {
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
    ContainerEditPage()
}
