package com.zktony.android.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Top app bar
 *
 * @param modifier Modifier
 * @param title String
 * @param onBack Function0<Unit>
 * @param onSetting [@androidx.compose.runtime.Composable] Function0<Unit>?
 * @return Unit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZkTonyTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    onBack: () -> Unit = {},
    onSetting: @Composable (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        navigationIcon = {
            FilledIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.surface,
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        actions = {
            onSetting?.invoke()
        }
    )
}

@Composable
@Preview
fun TopAppBarPreview() {
    ZkTonyTopAppBar(title = "Title")
}

