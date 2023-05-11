package com.zktony.android.ui.screen.container

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.R
import com.zktony.android.data.entity.Container
import com.zktony.android.ui.components.ZkTonyTopAppBar
import com.zktony.android.ui.viewmodel.ContainerPage
import kotlinx.coroutines.delay

/**
 * ContainerAddPage
 *
 * @param modifier Modifier
 * @param insert Function1<Container, Unit>
 * @param list List<Container>
 * @param navigationTo Function1<ContainerPage, Unit>
 * @param toggleSelected Function1<Long, Unit>
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ContainerAddPage(
    modifier: Modifier = Modifier,
    insert: (Container) -> Unit = {},
    list: List<Container> = emptyList(),
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
@Preview
fun ContainerAddPagePreview() {
    ContainerAddPage()
}