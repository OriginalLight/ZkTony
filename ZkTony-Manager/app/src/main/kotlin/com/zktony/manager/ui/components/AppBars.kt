package com.zktony.manager.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * @author: 刘贺贺
 * @date: 2023-02-17 11:26
 */

// region ManagerAppBar
@SuppressLint("ModifierParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerAppBar(
    modifier: Modifier = Modifier,
    title: String = "Title",
    isFullScreen: Boolean = false,
    onBack: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.inverseOnSurface
        ),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        navigationIcon = {
            if (isFullScreen) {
                FilledIconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(8.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    )
}
// endregion
