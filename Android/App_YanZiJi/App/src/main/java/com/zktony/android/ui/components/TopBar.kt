package com.zktony.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zktony.android.R
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.SettingsArgumentsViewModel

@Composable
fun SettingsArgumentsTopBar(
    modifier: Modifier = Modifier,
    viewModel: SettingsArgumentsViewModel
) {
    val navigationActions = LocalNavigationActions.current
    var showSecondConfirmation by remember { mutableStateOf(false) }

    if (showSecondConfirmation) {
        ImportSecondConfirmationDialog(
            onDismiss = { showSecondConfirmation = false },
            onConfirm = {
                viewModel.importArguments()
                showSecondConfirmation = false
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = { navigationActions.navigateUp() }
        ) {
            Icon(imageVector = Icons.AutoMirrored.Default.Reply, contentDescription = "Back")
        }

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { showSecondConfirmation = true }) {
                Text(text = stringResource(id = R.string.bring))
            }

            Button(onClick = { viewModel.exportArguments() }) {
                Text(text = stringResource(id = R.string.export))
            }
        }
    }
}