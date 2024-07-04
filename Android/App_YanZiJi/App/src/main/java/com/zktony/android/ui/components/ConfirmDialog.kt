package com.zktony.android.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.zktony.android.R

@Composable
fun ImportConfirmDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        title = { Text(stringResource(id = R.string.one_click_import)) },
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.import_confirm_content),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() }) { Text(stringResource(id = R.string.cancel)) }
        },
        confirmButton = {
            Button(onClick = { onConfirm() }) { Text(stringResource(id = R.string.ok)) }
        }
    )
}

@Composable
fun LogoutConfirmDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        title = { Text(stringResource(id = R.string.logout)) },
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.logout_confirm_content),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier,
        onDismissRequest = { onDismiss() },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() }) { Text(stringResource(id = R.string.cancel)) }
        },
        confirmButton = {
            Button(onClick = { onConfirm() }) { Text(stringResource(id = R.string.ok)) }
        }
    )
}