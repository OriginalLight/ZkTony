package com.zktony.android.ui.screen.container

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.R
import com.zktony.android.data.entity.Container
import com.zktony.android.ui.navigation.PageEnum
import com.zktony.core.ext.simpleDateFormat

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContainerMainPage(
    modifier: Modifier = Modifier,
    delete: (Container) -> Unit = {},
    entities: List<Container> = emptyList(),
    index: Int = -1,
    insert: (String) -> Unit = {},
    navigationTo: (PageEnum) -> Unit = {},
    toggleIndex: (Int) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(
            modifier = modifier.weight(1f),
        ) {
            val columnState = rememberLazyListState()

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
                entities.forEachIndexed { index1, it ->
                    item {
                        val background = if (index1 == index) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }

                        Card(
                            modifier = Modifier
                                .wrapContentHeight()
                                .clickable {
                                    if (index1 == index) {
                                        toggleIndex(-1)
                                    } else {
                                        toggleIndex(index1)
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = background),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(start = 16.dp),
                                    painter = painterResource(id = R.drawable.ic_container),
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
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.add),
                    )
                }

                AnimatedVisibility(visible = index != -1) {
                    var count by remember { mutableStateOf(0) }

                    FloatingActionButton(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        onClick = {
                            if (count == 1) {
                                delete(entities[index])
                                toggleIndex(-1)
                                count = 0
                            } else {
                                count++
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

                AnimatedVisibility(visible = index != -1) {
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
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            var name by remember { mutableStateOf("") }
            val softKeyboard = LocalSoftwareKeyboardController.current

            Row(
                modifier = Modifier
                    .height(128.dp)
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = MaterialTheme.shapes.medium
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                OutlinedTextField(
                    modifier = Modifier.width(400.dp),
                    value = name,
                    onValueChange = { name = it },
                    shape = MaterialTheme.shapes.large,
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center
                    ),
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.Abc,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier
                                .clickable { name = "" },
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = null,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        softKeyboard?.hide()
                    }),
                )

                AnimatedVisibility(visible = name.isNotBlank() && !entities.any { it.name == name }) {
                    FloatingActionButton(modifier = Modifier
                        .width(160.dp)
                        .padding(16.dp),
                        onClick = {
                            insert(name)
                            expanded = false
                            softKeyboard?.hide()
                        }) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun ContainerPagePreview() {
    ContainerMainPage(entities = listOf(Container()))
}