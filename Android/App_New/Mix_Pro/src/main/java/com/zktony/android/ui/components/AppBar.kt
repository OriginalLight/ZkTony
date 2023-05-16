package com.zktony.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.*
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Top app bar
 *
 * @param modifier Modifier
 * @param title String
 * @param navigation Function0<Unit>
 * @param actions [@androidx.compose.runtime.Composable] Function0<Unit>?
 * @return Unit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZkTonyTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    navigation: () -> Unit = {},
    actions: @Composable (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        modifier = modifier
            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            ),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
        navigationIcon = {
            FilledIconButton(
                onClick = navigation,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        actions = {
            actions?.invoke()
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun ZkTonyBottomAddAppBar(
    modifier: Modifier = Modifier,
    strings: List<String> = listOf(),
    insert: (String) -> Unit = {},
) {
    var name by remember { mutableStateOf("") }
    val softKeyboard = LocalSoftwareKeyboardController.current

    BottomAppBar(
        modifier = modifier
            .padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            ),
        windowInsets = WindowInsets.imeAnimationSource,
        containerColor = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
                        modifier = Modifier.clickable { name = "" },
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
            AnimatedVisibility(visible = name.isNotBlank() && !strings.contains(name)) {
                FloatingActionButton(
                    modifier = Modifier
                        .width(128.dp)
                        .padding(start = 16.dp),
                    onClick = {
                        insert(name)
                        softKeyboard?.hide()
                    }
                ) {
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

@Composable
@Preview
fun TopAppBarPreview() {
    ZkTonyTopAppBar(title = "Title")
}

@Composable
@Preview
fun BottomAppBarPreview() {
    ZkTonyBottomAddAppBar()
}

